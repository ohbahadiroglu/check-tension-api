package com.example.checktensionapi;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

@EnableScheduling
@SpringBootApplication
@EnableConfigurationProperties(ConfigProperties.class)
public class Application {

    private final RestService restService;

    public Application(RestService restService) {
        this.restService = restService;

    }

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);

    }



    @Scheduled(fixedDelayString ="${customprop.scheduleFixedDelay}")
    public void checkApi() throws Exception {
        // create a new post
        System.out.println(restService.createPost());
    }
}
