package com.flowablelab;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.flowablelab.**.infrastructure.mapper")
public class FlowableLabBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(FlowableLabBackendApplication.class, args);
    }
}
