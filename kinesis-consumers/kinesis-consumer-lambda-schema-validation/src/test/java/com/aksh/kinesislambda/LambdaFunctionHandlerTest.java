package com.aksh.kinesislambda;

import cloud.localstack.LocalstackTestRunner;
import cloud.localstack.docker.annotation.LocalstackDockerProperties;
import com.aksh.kinesislambda.dto.Customer;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.events.KinesisEvent;
import com.amazonaws.util.StringInputStream;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.util.Base64Utils;
import org.springframework.util.FileCopyUtils;

import java.io.FileReader;
import java.io.IOException;
import java.io.UncheckedIOException;

/**
 * A simple test harness for locally invoking your Lambda function handler.
 */
@RunWith(LocalstackTestRunner.class)
@LocalstackDockerProperties(services = { "dynamodb" })
public class LambdaFunctionHandlerTest {
	
	public static final String TABLE_NAME="dynamo-table";
	static ApplicationContext applicationContext;
	@BeforeClass
	public static void beforeClass() {
		
		System.setProperty("table.name", TABLE_NAME);
		applicationContext=new AnnotationConfigApplicationContext(UTBeanConfig.class);
		UTBeanConfig.createTable(applicationContext.getBean(AmazonDynamoDB.class), TABLE_NAME);
	}

	public KinesisEvent createInput(String payloadFile) {
		try {
			String payload = FileCopyUtils.copyToString(new FileReader(payloadFile));
			String kinesisEvent = FileCopyUtils.copyToString(new FileReader("src/test/resources/kinesis-event.json"))
					.replace("BASE_64_CONTENT", Base64Utils.encodeToString(payload.getBytes()));
			return TestUtils.parse(new StringInputStream(kinesisEvent), KinesisEvent.class);
		} catch (IOException e) {
			throw new UncheckedIOException(e);
		}
	}

	private Context createContext() {
		TestContext ctx = new TestContext();
		ctx.setFunctionName("MyFunctionName");
		return ctx;
	}

	@Test
	public void testLambdaFunctionHandler() {
		LambdaFunctionHandler handler = new LambdaFunctionHandler(applicationContext);
		Context ctx = createContext();

		Integer output = handler.handleRequest(createInput("src/test/resources/payload.json"), ctx);

	}
}
