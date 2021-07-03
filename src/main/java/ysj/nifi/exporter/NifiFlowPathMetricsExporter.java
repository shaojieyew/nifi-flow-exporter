package ysj.nifi.exporter;

import io.prometheus.client.Gauge;
import org.springframework.stereotype.Service;
import ysj.nifi.crawler.NifiMetricsCrawler;
import ysj.nifi.model.FlowPath;

import javax.security.auth.login.LoginException;
import java.io.IOException;
import java.util.Set;

@Service
public class NifiFlowPathMetricsExporter extends Exporter{
    static Gauge flowLatencyGauge;
    static Gauge flowCongestionGauge;
    static Gauge flowRunStateGauge;


    public NifiFlowPathMetricsExporter() {
        if(flowLatencyGauge==null){
            flowLatencyGauge =  Gauge.build()
                    .name("nifi_flow_latency")
                    .help("measure of latency in a chain of processors in millisec")
                    .labelNames("process_group", "process_group_id", "processor_path", "starting_process", "starting_process_id", "processor_name", "id")
                    .register();
        }
        if(flowCongestionGauge==null){
            flowCongestionGauge =  Gauge.build()
                    .name("nifi_flow_congestion")
                    .help("max congestion rate in a chain of processors")
                    .labelNames("process_group", "process_group_id", "processor_path", "starting_process", "starting_process_id", "processor_name", "id")
                    .register();
        }
        if(flowRunStateGauge==null){
            flowRunStateGauge =  Gauge.build()
                    .name("nifi_flow_running_state")
                    .help("running state of a flow path")
                    .labelNames("process_group", "process_group_id", "processor_path", "starting_process", "starting_process_id", "processor_name", "id")
                    .register();
        }
    }


    public void export() throws LoginException, IOException {
        Set<FlowPath> flowPaths = NifiMetricsCrawler.getFlowPath();

        for(FlowPath flow : flowPaths){
            String[] labels = {
                    flow.getEndComponent().getGroup(),
                    flow.getEndComponent().getGroupId(),
                    flow.getEndComponent().getFlowPath(),
                    flow.getStartComponent().getName(),
                    flow.getStartComponent().getId(),
                    flow.getEndComponent().getName(),
                    flow.getEndComponent().getId()
            };

            flowRunStateGauge.labels(labels).set(flow.getFlowPathStatus().isRunning()?1:0);
            flowLatencyGauge.labels(labels).set(flow.getFlowPathStatus().getLatency());
            flowCongestionGauge.labels(labels).set(flow.getFlowPathStatus().getCongestionRate());
        }

    }

}
