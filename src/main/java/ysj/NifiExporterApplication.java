package ysj;

import io.prometheus.client.exporter.MetricsServlet;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.context.annotation.Bean;
import ysj.nifi.crawler.Crawler;
import ysj.nifi.exporter.Exporter;

import java.util.List;

@SpringBootApplication
public class NifiExporterApplication extends SpringBootServletInitializer implements ApplicationRunner {

    @Autowired
    private List<Exporter> exporters;
    @Autowired
    private List<Crawler> crawlers;

    @Value("${exporter.address}") String url;

    @Bean
    public ServletRegistrationBean nifiExporterServlet() {
        return new ServletRegistrationBean(new MetricsServlet(), "/"+url);
    }

    public static void main(String[] args) {
        SpringApplication.run(NifiExporterApplication.class, args);
    }

    @Override
    public void run(ApplicationArguments args) {
        crawlers.forEach(Crawler::run);
        exporters.forEach(Exporter::run);
    }
}
