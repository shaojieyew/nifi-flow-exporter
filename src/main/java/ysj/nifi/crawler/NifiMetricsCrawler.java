package ysj.nifi.crawler;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ysj.nifi.NifiService;
import ysj.nifi.model.FlowPath;

import javax.security.auth.login.LoginException;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

@Service
public class NifiMetricsCrawler extends Crawler{
    @Autowired
    NifiService nifiService;


    static Set<FlowPath> flowPath = new HashSet<>();

    public static Set<FlowPath> getFlowPath() {
        return flowPath;
    }


    public void crawl() throws IOException, LoginException {
        flowPath = nifiService.getFlowsMetrics();
    }

}
