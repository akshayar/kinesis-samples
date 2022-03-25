package com.aksh.kinesislambda;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

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
