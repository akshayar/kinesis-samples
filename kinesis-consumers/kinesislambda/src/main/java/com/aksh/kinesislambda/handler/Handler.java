/**
 * 
 */
package com.aksh.kinesislambda.handler;

import java.util.Optional;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.aksh.kinesislambda.dao.CustomerDao;
import com.aksh.kinesislambda.dto.Customer;
import com.amazonaws.services.lambda.runtime.events.KinesisEvent.KinesisEventRecord;
import com.google.gson.Gson;

import lombok.extern.slf4j.Slf4j;

/**
 * @author aksrawat
 *
 */
@Component
@Slf4j
public class Handler {
	@Autowired
	CustomerDao dao;
	
	Gson gson=new Gson();
	
	@Value("${table.name}")
	private String tableName;

	@PostConstruct
	public void init() {
		tableName=Optional.ofNullable(System.getenv("tableName")).orElse(tableName);
	}
	public void handle(KinesisEventRecord record) {
		String payload = new String(record.getKinesis().getData().array());
		log.info("Payload: " + payload);
		dao.save(gson.fromJson(payload, Customer.class), tableName);
	}

}
