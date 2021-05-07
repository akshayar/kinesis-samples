package com.aksh.kinesislambda.dao;

import java.util.Arrays;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.aksh.kinesislambda.UTBeanConfig;
import com.aksh.kinesislambda.dto.Customer;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.model.AttributeDefinition;
import com.amazonaws.services.dynamodbv2.model.CreateTableRequest;
import com.amazonaws.services.dynamodbv2.model.KeySchemaElement;
import com.amazonaws.services.dynamodbv2.model.KeyType;

import cloud.localstack.LocalstackTestRunner;
import cloud.localstack.TestUtils;
import cloud.localstack.docker.annotation.LocalstackDockerProperties;

@RunWith(LocalstackTestRunner.class)
@LocalstackDockerProperties(services = { "dynamodb" })
public class CustomerDaoTest {
	static final String TABLE_NAME = "dynamo-customer-data";
	CustomerDao customerDao = new CustomerDao();
	static AmazonDynamoDB dynamodb = TestUtils.getClientDynamoDB();

	@BeforeClass
	public static void beforeClass() {
		UTBeanConfig.createTable(dynamodb, TABLE_NAME);
	}

	@Before
	public void before() {
		customerDao.setDynamoDB(dynamodb);
		customerDao.init();
	}

	@Test
	public void testSave() {
		Customer customer = new Customer();
		customer.setId("id1");
		customer.setName("Name");
		customer.setAge(10);
		;
		customerDao.save(customer, TABLE_NAME);
		Customer customerRet = customerDao.get("id1", TABLE_NAME);
		org.junit.Assert.assertEquals(customer, customerRet);

	}

}
