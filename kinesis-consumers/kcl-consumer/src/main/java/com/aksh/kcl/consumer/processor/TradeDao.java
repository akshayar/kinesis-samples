package com.aksh.kcl.consumer.processor;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;

import lombok.extern.java.Log;
@Component
@Log
public class TradeDao {
	@Autowired
	private AmazonDynamoDB client;
	
	private DynamoDBMapper mapper;
	
	@PostConstruct
	public void init() {
		mapper=new DynamoDBMapper(client);
	}
	
	@PreDestroy
	public void destroy() {
		log.info("I am dying save yourself");
	}
	
	public void save(Trade trade) {
		mapper.save(trade);
	}
	
	public Trade getById(String tradeId) {
		return mapper.load(Trade.class,tradeId);
	}

}
