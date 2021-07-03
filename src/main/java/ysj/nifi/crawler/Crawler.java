package ysj.nifi.crawler;

import org.springframework.beans.factory.annotation.Value;

import javax.security.auth.login.LoginException;
import java.io.IOException;

public abstract class Crawler {

    @Value("${crawl.interval.ms}") long interval;

    abstract void crawl() throws Exception;


    public void run()  {
        new Thread(() -> {
            while(true){
                try {
                    crawl();
                    Thread.sleep(interval);
                } catch (LoginException | IOException | InterruptedException e) {
                    e.printStackTrace();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }
}
