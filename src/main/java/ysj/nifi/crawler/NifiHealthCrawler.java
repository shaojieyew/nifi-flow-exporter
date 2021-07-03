package ysj.nifi.crawler;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import ysj.nifi.NifiService;
import ysj.nifi.model.ClusterSummaryEntity;
import ysj.nifi.model.FlowPath;

import javax.security.auth.login.LoginException;
import java.io.IOException;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@Service
public class NifiHealthCrawler extends Crawler{
    @Autowired
    NifiService nifiService;


    static ClusterSummaryEntity.ClusterSummaryDTO clusterSummaryDTO ;
    static Map<String, Long> clusterMetrics ;

    public static ClusterSummaryEntity.ClusterSummaryDTO getClusterSummaryDTO() {
        return clusterSummaryDTO;
    }

    public static Map<String, Long> getClusterMetrics() {
        return clusterMetrics;
    }

    public void crawl() throws IOException, LoginException {
        clusterSummaryDTO = nifiService.getClusterSummary();
        clusterMetrics = nifiService.getClusterStatus();
    }


}
