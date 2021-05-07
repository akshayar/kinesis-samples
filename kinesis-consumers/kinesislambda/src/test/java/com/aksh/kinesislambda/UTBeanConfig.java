package com.aksh.kinesislambda;

import java.util.Arrays;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.amazonaws.services.dynamodbv2.model.AttributeDefinition;
import com.amazonaws.services.dynamodbv2.model.CreateTableRequest;
import com.amazonaws.services.dynamodbv2.model.KeySchemaElement;
import com.amazonaws.services.dynamodbv2.model.KeyType;

import cloud.localstack.TestUtils;

/**
 * @author aksrawat
 *
 */
@Configuration
@ComponentScan(basePackages = { "com.aksh.kinesislambda" })
public class UTBeanConfig {
	
	@Bean
	public AmazonDynamoDB dynamoDB() {
		return  TestUtils.getClientDynamoDB();
	}
	
	public static void createTable(AmazonDynamoDB dynamoDb,String table) {
		CreateTableRequest req = new CreateTableRequest();
		req.setTableName(table);
		req.setAttributeDefinitions(Arrays.asList(new AttributeDefinition("id", "S")));
		req.setKeySchema(Arrays.asList(new KeySchemaElement("id", KeyType.HASH)));
		req.setBillingMode("PAY_PER_REQUEST");
		dynamoDb.createTable(req);
	}
}
