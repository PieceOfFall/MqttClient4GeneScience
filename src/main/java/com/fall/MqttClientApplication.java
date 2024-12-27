package com.fall;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@EnableAsync
@SpringBootApplication
public class MqttClientApplication {
    public static void main(String[] args) {
        SpringApplication.run(MqttClientApplication.class,args);
    }
}