package ysj.nifi.exporter;

import com.davis.client.model.PortStatusSnapshotDTO;
import com.davis.client.model.ProcessorStatusSnapshotDTO;
import io.prometheus.client.Gauge;
import org.springframework.stereotype.Service;
import ysj.nifi.crawler.NifiMetricsCrawler;
import ysj.nifi.model.FlowPath;

import javax.security.auth.login.LoginException;
import java.io.IOException;
import java.util.Set;

@Service
public class NifiFlowFileInCountExporter extends Exporter{
    static Gauge flowFileGauge;


    public NifiFlowFileInCountExporter() {
        if(flowFileGauge ==null){
            flowFileGauge =  Gauge.build()
                    .name("nifi_flow_file_metrics")
                    .help("number of flowfile in and out / byte over 5min")
                    .labelNames("process_group", "process_group_id", "processor_path", "processor_type", "starting_process", "starting_process_id", "starting_process_type", "processor_name", "id", "type")
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
                    flow.getEndComponent().getType(),
                    flow.getStartComponent().getName(),
                    flow.getStartComponent().getId(),
                    flow.getStartComponent().getType(),
                    flow.getEndComponent().getName(),
                    flow.getEndComponent().getId()
            };

            String []labelBytesRead = new String[labels.length+1];
            String []labelBytesWritten = new String[labels.length+1];
            String []labelFlowFilesIn = new String[labels.length+1];
            String []labelFlowFilesOut = new String[labels.length+1];
            System.arraycopy(labels, 0, labelBytesRead, 0,labels.length);
            System.arraycopy(labels, 0, labelBytesWritten, 0,labels.length);
            System.arraycopy(labels, 0, labelFlowFilesIn, 0,labels.length);
            System.arraycopy(labels, 0, labelFlowFilesOut, 0,labels.length);
            labelBytesRead[labelBytesRead.length-1] = "BytesRead";
            labelBytesWritten[labelBytesWritten.length-1] = "BytesWritten";
            labelFlowFilesIn[labelFlowFilesIn.length-1] = "FlowFilesIn";
            labelFlowFilesOut[labelFlowFilesOut.length-1] = "FlowFilesOut";


            if(flow.getEndComponent().getSnapshot() instanceof ProcessorStatusSnapshotDTO){
                ProcessorStatusSnapshotDTO processorStatusSnapshotDTO = (ProcessorStatusSnapshotDTO) flow.getEndComponent().getSnapshot();

                flowFileGauge.labels(labelFlowFilesIn).set(processorStatusSnapshotDTO.getFlowFilesIn());
                flowFileGauge.labels(labelFlowFilesOut).set(processorStatusSnapshotDTO.getFlowFilesOut());
                flowFileGauge.labels(labelBytesRead).set(processorStatusSnapshotDTO.getBytesRead());
                flowFileGauge.labels(labelBytesWritten).set(processorStatusSnapshotDTO.getBytesWritten());

            }else{
                if(flow.getEndComponent().getSnapshot() instanceof PortStatusSnapshotDTO){
                    PortStatusSnapshotDTO portStatusSnapshotDTO = (PortStatusSnapshotDTO) flow.getEndComponent().getSnapshot();
                    flowFileGauge.labels(labelFlowFilesIn).set(portStatusSnapshotDTO.getFlowFilesIn());
                    flowFileGauge.labels(labelFlowFilesOut).set(portStatusSnapshotDTO.getFlowFilesOut());
                }
            }

        }

    }

}
