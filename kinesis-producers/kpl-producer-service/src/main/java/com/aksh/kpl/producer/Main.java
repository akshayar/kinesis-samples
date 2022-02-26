package com.aksh.kpl.producer;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import software.amazon.awssdk.regions.Region;

@SpringBootApplication
public class Main {

    @Value("${region:us-east-1}")
    private String regionString;

    public static void main(String[] args) {
        SpringApplication.run(Main.class, args);
    }

    @Bean
    public Region region() {
       return Region.of(regionString);
    }

}
