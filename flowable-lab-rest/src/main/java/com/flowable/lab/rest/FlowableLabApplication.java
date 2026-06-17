package com.flowable.lab.rest;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackages = "com.flowable.lab")
public class FlowableLabApplication {

    public static void main(String[] args) {
        SpringApplication.run(FlowableLabApplication.class, args);
    }
}
