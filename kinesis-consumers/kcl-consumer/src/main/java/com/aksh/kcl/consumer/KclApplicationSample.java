/**
 * 
 */
package com.aksh.kcl.consumer;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;

import com.amazonaws.AmazonClientException;
import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.auth.EC2ContainerCredentialsProviderWrapper;
import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.kinesis.AmazonKinesis;
import com.amazonaws.services.kinesis.AmazonKinesisClientBuilder;

import lombok.extern.java.Log;

/**
 * Sample Amazon Kinesis Application.
 */
@SpringBootApplication
@Log
public class KclApplicationSample {

	@Value("${region:ap-south-1}")
	private String region;

	@Autowired
	private AWSCredentialsProvider credentialsProvider;

	public static void main(String[] args) {
		SpringApplication.run(KclApplicationSample.class, args);
	}

	@Bean
	public AmazonKinesis kinesis() {
		return AmazonKinesisClientBuilder.standard().withCredentials(credentialsProvider).withRegion(region).build();
	}

	@Bean
	public AmazonDynamoDB dynamo() {
		return AmazonDynamoDBClientBuilder.standard().withCredentials(credentialsProvider).withRegion(region).build();
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

	@Bean
	public AWSCredentialsProvider initCredentials() {
		if (!Optional.ofNullable(System.getProperty("server.host")).isPresent()) {
			log.info("server.host "+System.getProperty("server.host"));
			return new EC2ContainerCredentialsProviderWrapper();
		} else {
			// Ensure the JVM will refresh the cached IP values of AWS resources (e.g. //
			// service endpoints).
			java.security.Security.setProperty("networkaddress.cache.ttl", "60");

			/*
			 * The ProfileCredentialsProvider will return your [default] credential profile
			 * by reading from the credentials file located at (~/.aws/credentials).
			 */

			ProfileCredentialsProvider credentialsProvider = new ProfileCredentialsProvider();
			try {
				credentialsProvider.getCredentials();
			} catch (Exception e) {
				throw new AmazonClientException("Cannot load the credentials from the credential profiles file. "
						+ "Please make sure that your credentials file is at the correct "
						+ "location (~/.aws/credentials), and is in valid format.", e);
			}
			return credentialsProvider;

		}

	}
}
