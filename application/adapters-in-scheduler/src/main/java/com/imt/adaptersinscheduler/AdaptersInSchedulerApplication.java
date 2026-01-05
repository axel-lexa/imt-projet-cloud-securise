package com.imt.adaptersinscheduler;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class AdaptersInSchedulerApplication {

    public static void main(String[] args) {
        SpringApplication.run(AdaptersInSchedulerApplication.class, args);
    }

}
