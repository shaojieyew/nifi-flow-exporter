package ysj.nifi;

import com.davis.client.model.StatusSnapshotDTO;
import org.apache.commons.lang.StringUtils;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;
import ysj.nifi.model.*;

import javax.security.auth.login.LoginException;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class NifiService {

    public static void main(String[] arg) throws Exception {
        NifiService nifiService = new NifiService("http://localhost:8080", null, null, null, null);
//        Set<FlowPath> flows = nifiService.getFlowsMetrics();
//        flows.forEach(flow->{
//            NifiComponent startComponent = flow.getStartComponent();
//            NifiComponent endComponent = flow.getEndComponent();
//            System.out.println(startComponent.getGroup()+":"+startComponent.getName()+"->"+endComponent.getGroup()+":"+endComponent.getName());
//            System.out.println(flow.getFlowPathStatus());
//            System.out.println("");
//        });

    }

    private final NifiApiClient nifiApiClient;

    public enum ProcessType {
        CONNECTION,
        INPUT_PORT,
        OUTPUT_PORT,
        PROCESS_GROUP
    }

    public enum ProcessorStatus {
        DISABLED,
        RUNNING,
        STOPPED
    }

    public NifiService(@Value("${nifi.url}") String nifiHost,
                       @Value("${nifi.username}") String username,
                       @Value("${nifi.password}") String password,
                       @Value("${nifi.kerberos.keytab}") String keytab,
                       @Value("${nifi.kerberos.principal}") String principle) {
        if(ObjectUtils.isEmpty(nifiHost)){
            throw new RuntimeException("nifi.url is empty");
        }

        if((!ObjectUtils.isEmpty(username) && !ObjectUtils.isEmpty(password))){
            nifiApiClient = new NifiApiClient( nifiHost,  username,  password);
        }else{
            if((!ObjectUtils.isEmpty(keytab) && !ObjectUtils.isEmpty(principle))){
                nifiApiClient = new NifiApiClient( nifiHost,  principle,  new File(keytab));
            }else{
                nifiApiClient = new NifiApiClient( nifiHost);
            }
        }
    }

    public Map<String, Long> getClusterStatus() throws IOException, LoginException {
        Map <String, Long> metrics = new HashMap();
        DateTime curr = DateTime.now().minusHours(1);
        List<ControllerSnapshotEntity> statuses = nifiApiClient.getClusterStatus()
                .getStatusHistory()
                .getAggregateSnapshots();

        for(ControllerSnapshotEntity c: statuses){
            if(c.getTimestamp().isAfter(curr)){
                curr = c.getTimestamp();
                metrics = c.getStatusMetrics();
            }
        }
        return metrics;
    }

    public Set<NifiComponent> findNifiComponent(String pattern, String processType) throws IOException, LoginException {
        if(processType==null){
            processType = "";
        }
        if(pattern==null){
            pattern="";
        }
        String finalPattern = pattern;
        String finalProcessType = processType;

        ProcessGroupStatusEntityV2 summary = nifiApiClient.getSummary();
        return getAllNifiComponent(null, null, summary.getProcessGroupStatus().getAggregateSnapshot())
                .filter(nifiComponent ->{
                    boolean matchingProcessType = true;
                    if(!StringUtils.isEmpty(finalProcessType)){
                        matchingProcessType = finalProcessType.equalsIgnoreCase(nifiComponent.getType());
                    }
                    if(finalPattern.equals(nifiComponent.getId())
                            || finalPattern.equals(nifiComponent.getGroupId())){
                        return matchingProcessType;
                    }

                    return Pattern.compile(finalPattern.trim()).matcher(nifiComponent.getFlowPath()).find() && matchingProcessType;
                })
                .collect(Collectors.toSet());
    }

    static long getFlowsMetricsLastCalled = 0;
    static Set<FlowPath> flowPathCache;

    public ClusterSummaryEntity.ClusterSummaryDTO getClusterSummary() throws IOException, LoginException {
        return nifiApiClient.getClusterInfoJson().getClusterSummary();
    }

    public Set<FlowPath> getFlowsMetrics() throws LoginException, IOException {
        if(flowPathCache!=null && System.currentTimeMillis()-getFlowsMetricsLastCalled<1000L*10){
            return flowPathCache;
        }
        getFlowsMetricsLastCalled = System.currentTimeMillis();
        Map<String, NifiComponent> graph = new HashMap<>();
        ProcessGroupStatusEntityV2 summary = nifiApiClient.getSummary();
        ProcessGroupStatusSnapshotDTOV2 processGroupStatusSnapshotDTO =  summary.getProcessGroupStatus().getAggregateSnapshot();
        List<NifiComponent> components= getAllNifiComponent(null, null, processGroupStatusSnapshotDTO).collect(Collectors.toList());
        components.forEach(component->graph.put(component.getId(), component));

        Stream<ConnectionEntityV2> connectionEntityStream = Stream.concat(components.stream()
                .filter(c->c.getType().equalsIgnoreCase(ProcessType.PROCESS_GROUP.name()))
                .flatMap(g-> {
                    try {
                        return nifiApiClient.getProcessGroup(g.getId()).getProcessGroupFlow().getFlow().getConnections().stream();
                    } catch (LoginException | IOException e) {
                        e.printStackTrace();
                    }
                    return Stream.empty();
                }),nifiApiClient.getProcessGroup("root").getProcessGroupFlow().getFlow().getConnections().stream());

        Set<String> processorIdsWithNoInput = new HashSet<>();
        for (NifiComponent c : components) {
            if (Arrays.stream(ProcessType.values()).map(Enum::name)
                    .noneMatch(s -> (s.equalsIgnoreCase(c.getType())))) {
                processorIdsWithNoInput.add(c.getId());
            }
            if ((c.getType().equalsIgnoreCase(ProcessType.OUTPUT_PORT.name()))
                            || (c.getType().equalsIgnoreCase(ProcessType.INPUT_PORT.name()))) {
                processorIdsWithNoInput.add(c.getId());
            }
        }
        connectionEntityStream.forEach(connection->{
            NifiComponent srcComponent = graph.get(connection.getSourceId());
            NifiComponent dstComponent = graph.get(connection.getDestinationId());
            processorIdsWithNoInput.remove(dstComponent.getId());
            NifiComponent conn = new NifiComponent();
            conn.setSnapshot(connection);
            conn.setType(ProcessType.CONNECTION.name());
            conn.setName(connection.getStatus().getName());
            conn.setId(connection.getId());
            conn.setFlowPath(srcComponent.getFlowPath()+"/"+connection.getStatus().getName());
            conn.setGroup(srcComponent.getGroup());
            conn.setGroupId(srcComponent.getGroupId());
            conn.setStatus(dstComponent.getStatus());
            conn.setSnapshot(connection.getStatus().getAggregateSnapshot());
            conn.getNextComponent().add(dstComponent);

            srcComponent.getNextComponent().add(conn);
        });
        Set<FlowPath> flowPaths = new HashSet<>();
        processorIdsWithNoInput.forEach(id -> {
            NifiComponent component = graph.get(id);
            try {
                FlowPathStatus flowPathStatus = new FlowPathStatus(component.getStatus().equalsIgnoreCase(ProcessorStatus.RUNNING.name()),0,0);
                FlowPath flowPath = new FlowPath(component, component, flowPathStatus);
                flowPaths.add(flowPath);
                traverseGraph(component, component , flowPathStatus, flowPaths);
            } catch (LoginException | IOException e) {
                e.printStackTrace();
            }
        });
        flowPathCache= flowPaths;
        return flowPaths;
    }

    private OptionalDouble getAverageLineageDuration(String id) throws IOException, LoginException {
        List<StatusSnapshotDTO> historyStatus = nifiApiClient.getProcessHistoryJson(id).getStatusHistory().getAggregateSnapshots();

        return historyStatus.stream().filter(statusSnapshotDTO ->
            statusSnapshotDTO.getTimestamp().isAfter(DateTime.now().minusMinutes(5))
        ).map(StatusSnapshotDTO::getStatusMetrics)
                .filter(map -> map.get("taskCount")>0).map(map->map.get("averageLineageDuration"))
                .mapToDouble(d -> d)
                .average();
    }

    public void traverseGraph(NifiComponent init, NifiComponent component, FlowPathStatus accumulatedInfo,Set<FlowPath> flowPaths) throws LoginException, IOException {
        if(!ObjectUtils.isEmpty(component.getNextComponent())){
            for(NifiComponent c: component.getNextComponent()){
                boolean isRunning = accumulatedInfo.isRunning() && c.getStatus().equals(ProcessorStatus.RUNNING.name());
                long congestionRate = accumulatedInfo.getCongestionRate();
                long latency = accumulatedInfo.getLatency();
                if(c.getType().equals(ProcessType.CONNECTION.name())){
                    congestionRate = Math.max(accumulatedInfo.getCongestionRate(), ((ConnectionStatusSnapshotDTOV2)c.getSnapshot()).getPercentUseCount());
                }else{
                    latency = (long) getAverageLineageDuration(c.getId()).orElse(latency);
                }
                FlowPathStatus newAccumulatedInfo = new FlowPathStatus(isRunning, congestionRate, latency);
                if(!c.getType().equals(ProcessType.CONNECTION.name())){
                    FlowPath flowPath = new FlowPath(init, c, newAccumulatedInfo);
                    flowPaths.add(flowPath);
                }
                traverseGraph(init, c, newAccumulatedInfo, flowPaths);
            }
        }
    }


    public Stream<NifiComponent> getAllNifiComponent(String flowPath,String groupName, ProcessGroupStatusSnapshotDTOV2 processGroupStatusSnapshotDTO)  {
        if(groupName==null){
            groupName="";
        }
        if(flowPath==null){
            flowPath="";
        }
        String finalGroupName = groupName;
        String finalFlowPath = flowPath;

        Stream<NifiComponent> nifiComponentStream = processGroupStatusSnapshotDTO.getProcessorStatusSnapshots()
                .stream().map(processor->{
                            String name = processor.getProcessorStatusSnapshot().getName();
                            NifiComponent nifiComponent = new NifiComponent();
                            nifiComponent.setFlowPath( finalFlowPath+"/"+ name);
                            nifiComponent.setName(name);
                            nifiComponent.setGroupId(processor.getProcessorStatusSnapshot().getGroupId());
                            nifiComponent.setGroup(finalGroupName.length()==0?"root":finalGroupName);
                            nifiComponent.setId(processor.getId());
                            nifiComponent.setType(processor.getProcessorStatusSnapshot().getType());
                            nifiComponent.setStatus(String.valueOf(processor.getProcessorStatusSnapshot().getRunStatus()));
                            nifiComponent.setSnapshot(processor.getProcessorStatusSnapshot());
                            return nifiComponent;
                        }
                );
        Stream<NifiComponent> nifiComponentInportStream = processGroupStatusSnapshotDTO.getInputPortStatusSnapshots()
                .stream().map(processor->{
                            String name = processor.getPortStatusSnapshot().getName();
                            NifiComponent nifiComponent = new NifiComponent();
                            nifiComponent.setFlowPath( finalFlowPath+"/"+ name);
                            nifiComponent.setName(name);
                            nifiComponent.setGroupId(processor.getPortStatusSnapshot().getGroupId());
                            nifiComponent.setGroup(finalGroupName.length()==0?"root":finalGroupName);
                            nifiComponent.setId(processor.getId());
                            nifiComponent.setType(ProcessType.INPUT_PORT.name());
                            nifiComponent.setStatus(String.valueOf(processor.getPortStatusSnapshot().getRunStatus()));
                            nifiComponent.setSnapshot(processor.getPortStatusSnapshot());
                            return nifiComponent;
                        }
                );
        Stream<NifiComponent> nifiComponentOutputPortStream = processGroupStatusSnapshotDTO.getOutputPortStatusSnapshots()
                .stream().map(processor->{
                            String name = processor.getPortStatusSnapshot().getName();
                            NifiComponent nifiComponent = new NifiComponent();
                            nifiComponent.setFlowPath( finalFlowPath+"/"+ name);
                            nifiComponent.setName(name);
                            nifiComponent.setGroupId(processor.getPortStatusSnapshot().getGroupId());
                            nifiComponent.setGroup(finalGroupName.length()==0?"root":finalGroupName);
                            nifiComponent.setId(processor.getId());
                            nifiComponent.setType(ProcessType.OUTPUT_PORT.name());
                            nifiComponent.setStatus(String.valueOf(processor.getPortStatusSnapshot().getRunStatus()));
                            nifiComponent.setSnapshot(processor.getPortStatusSnapshot());
                            return nifiComponent;
                        }
                );
        Stream<NifiComponent> nestedNifiComponentStream = processGroupStatusSnapshotDTO.getProcessGroupStatusSnapshots().stream()
                .flatMap(pg->{
                    NifiComponent nifiComponent = new NifiComponent();
                    nifiComponent.setName(pg.getProcessGroupStatusSnapshot().getName());
                    nifiComponent.setId(pg.getId());
                    nifiComponent.setType(ProcessType.PROCESS_GROUP.name());
                    nifiComponent.setGroup(finalGroupName.length()==0?"root":finalGroupName);
                    nifiComponent.setFlowPath(finalFlowPath +"/"+pg.getProcessGroupStatusSnapshot().getName());
                    nifiComponent.setGroupId(processGroupStatusSnapshotDTO.getId());
                    nifiComponent.setSnapshot(pg.getProcessGroupStatusSnapshot());
                    NifiComponent[] group = {nifiComponent};
                    return Stream.concat(Arrays.stream(group), getAllNifiComponent(finalFlowPath +"/"+pg.getProcessGroupStatusSnapshot().getName(),pg.getProcessGroupStatusSnapshot().getName(),pg.getProcessGroupStatusSnapshot()));
                });

        return Stream.concat(Stream.concat(Stream.concat(nifiComponentInportStream, nifiComponentOutputPortStream), nestedNifiComponentStream), nifiComponentStream);
    }
}
