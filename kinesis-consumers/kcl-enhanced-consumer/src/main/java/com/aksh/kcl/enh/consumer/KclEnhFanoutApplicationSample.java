/**
 * 
 */
package com.aksh.kcl.enh.consumer;

import java.util.Arrays;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;

import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.cloudwatch.CloudWatchAsyncClient;
import software.amazon.awssdk.services.dynamodb.DynamoDbAsyncClient;
import software.amazon.awssdk.services.kinesis.KinesisAsyncClient;
import software.amazon.kinesis.common.KinesisClientUtil;

/**
 * Sample Amazon Kinesis Application.
 */
@SpringBootApplication
public  class KclEnhFanoutApplicationSample {
	
	@Value("${region:us-east-1}")
	private String regionString;
	
	@Autowired
	private Region region;
	
	
	public static void main(String[] args) {
		SpringApplication.run(KclEnhFanoutApplicationSample.class, args);
	}
	
	@Bean
	public CloudWatchAsyncClient cloudWatch() {
		return  CloudWatchAsyncClient.builder().region(region).build();
	}
	
	@Bean
	public DynamoDbAsyncClient dynamo() {
		return DynamoDbAsyncClient.builder().region(region).build();
	}
	@Bean
	public Region region() {
		return Region.of(regionString);
	}

	@Bean
	public KinesisAsyncClient kinesisClient() {
		return KinesisClientUtil
		.createKinesisAsyncClient(KinesisAsyncClient.builder().region(this.region));
	}
	
	@Bean
	public CommandLineRunner commandLineRunner(ApplicationContext ctx) {
		return args -> {

			/*
			 * System.out.println("Let's inspect the beans provided by Spring Boot:");
			 * 
			 * String[] beanNames = ctx.getBeanDefinitionNames(); Arrays.sort(beanNames);
			 * for (String beanName : beanNames) { System.out.println(beanName); }
			 */

		};
	}
}
