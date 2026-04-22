package com.springboot;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class SpringbootAssignmentApplication {
    public static void main(String[] args) {
        SpringApplication.run(SpringbootAssignmentApplication.class, args);
    }
}