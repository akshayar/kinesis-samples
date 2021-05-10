/**
 * 
 */
package com.aksh.kinesislambda.dao;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.aksh.kinesislambda.dto.Customer;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapperConfig;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapperConfig.TableNameOverride;

/**
 * @author aksha
 *
 */
@Component
public class CustomerDao {

	@Autowired
	AmazonDynamoDB dynamoDB;

	DynamoDBMapper mapper;

	Map<String, DynamoDBMapperConfig> mapperConfigMap = new ConcurrentHashMap<String, DynamoDBMapperConfig>();
	
	@PostConstruct
	public void init() {
		mapper=new DynamoDBMapper(dynamoDB);
	}

	public void save(Customer object, String table) {
		DynamoDBMapperConfig mapperConfig = mapperConfigMap.putIfAbsent(table, DynamoDBMapperConfig.builder()
				.withTableNameOverride(TableNameOverride.withTableNameReplacement(table)).build());
		if(mapperConfig ==null) {
			mapperConfig=mapperConfigMap.get(table);
		}
		mapper.save(object, mapperConfig);
		
	}

	public Customer get(String id, String table) {
		DynamoDBMapperConfig mapperConfig = mapperConfigMap.putIfAbsent(table, DynamoDBMapperConfig.builder()
				.withTableNameOverride(TableNameOverride.withTableNameReplacement(table)).build());
		if(mapperConfig ==null) {
			mapperConfig=mapperConfigMap.get(table);
		}
		return mapper.load(Customer.class, id,mapperConfig);
	}

	public void setDynamoDB(AmazonDynamoDB dynamoDB) {
		this.dynamoDB = dynamoDB;
	}

}
