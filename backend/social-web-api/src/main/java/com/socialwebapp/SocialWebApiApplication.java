package com.socialwebapp;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan // scans @ConfigurationProperties types under com.socialwebapp.*
public class SocialWebApiApplication {
    public static void main(String[] args) {
        SpringApplication.run(SocialWebApiApplication.class, args);
    }
}