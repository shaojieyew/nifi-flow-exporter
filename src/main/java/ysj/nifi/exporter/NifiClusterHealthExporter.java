package ysj.nifi.exporter;

import io.prometheus.client.Gauge;
import org.springframework.stereotype.Service;
import ysj.nifi.crawler.NifiHealthCrawler;

import java.util.Map;

@Service
public class NifiClusterHealthExporter extends Exporter {
    static Gauge connectedNodeGauge;
    static Gauge spaceUsedGauge;
    static Gauge freeSpaceGauge;
    static Gauge totalThreadGauge;
    static Gauge heapUsedGauge;
    static Gauge freeHeapGauge;

    public NifiClusterHealthExporter() {
        if (connectedNodeGauge == null) {
            connectedNodeGauge = Gauge.build()
                    .name("nifi_connected_node_count")
                    .help("number of node connected")
                    .register();
        }
        if (spaceUsedGauge == null) {
            spaceUsedGauge = Gauge.build()
                    .name("nifi_cluster_space_utilization")
                    .help("percentage of the storage space used")
                    .register();
        }
        if (freeSpaceGauge == null) {
            freeSpaceGauge = Gauge.build()
                    .name("nifi_cluster_free_space")
                    .help("The usable space (byte) available for use by the underlying storage mechanism")
                    .register();
        }
        if (totalThreadGauge == null) {
            totalThreadGauge = Gauge.build()
                    .name("nifi_cluster_live_thread")
                    .help("The current number of live threads in the Java virtual machine (both daemon and non-daemon threads).")
                    .register();
        }
        if (heapUsedGauge == null) {
            heapUsedGauge = Gauge.build()
                    .name("nifi_cluster_heap_utilization")
                    .help("percentage of the heap used")
                    .register();
        }
        if (freeHeapGauge == null) {
            freeHeapGauge = Gauge.build()
                    .name("nifi_cluster_free_heap")
                    .help("The amount of free memory (byte) in the heap that can be used by the Java virtual machine.")
                    .register();
        }
    }

    public void export()  {
        if(NifiHealthCrawler.getClusterSummaryDTO()!=null){
            if (NifiHealthCrawler.getClusterSummaryDTO().isClustered()){
                connectedNodeGauge.set(NifiHealthCrawler.getClusterSummaryDTO().getConnectedNodeCount());
            }else{
                connectedNodeGauge.set(1);
            }
        }
        Map<String, Long> metrics = NifiHealthCrawler.getClusterMetrics();
        if(metrics!=null){
            Long availableSpace = metrics.get("contentRepositoryFreeSpace");
            Long usedSpace = metrics.get("flowFileRepositoryUsedSpace");
            freeSpaceGauge.set(availableSpace);
            spaceUsedGauge.set((usedSpace/(availableSpace+usedSpace)));

            Long freeHeap = metrics.get("freeHeap");
            Long usedHeap = metrics.get("heapUtilization");
            freeHeapGauge.set(freeHeap);
            heapUsedGauge.set(usedHeap);

            totalThreadGauge.set(metrics.get("totalThreads"));
        }

    }
}
