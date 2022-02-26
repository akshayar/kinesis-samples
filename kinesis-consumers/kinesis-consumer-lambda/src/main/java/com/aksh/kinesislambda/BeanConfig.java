package com.aksh.kinesislambda;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;

/**
 * @author aksrawat
 *
 */
@Configuration
@ComponentScan(basePackages = { "com.aksh.kinesislambda" })
public class BeanConfig {

	@Bean
	public AmazonDynamoDB dynamoDB() {
		return AmazonDynamoDBClientBuilder.defaultClient();
	}

}
