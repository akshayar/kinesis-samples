package com.aksh.kinesislambda;

import java.io.FileReader;
import java.io.IOException;
import java.io.UncheckedIOException;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.util.Base64Utils;
import org.springframework.util.FileCopyUtils;

import com.aksh.kinesislambda.dao.CustomerDao;
import com.aksh.kinesislambda.dto.Customer;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.events.KinesisEvent;
import com.amazonaws.util.StringInputStream;

/**
 * A simple test harness for locally invoking your Lambda function handler.
 */
public class LambdaFunctionHandlerIntegrationTest {
	
	public static final String TABLE_NAME="dynamo-table";
	@BeforeClass
	public static void beforeClass() {
		System.setProperty("table.name", TABLE_NAME);
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
		LambdaFunctionHandler handler = new LambdaFunctionHandler();
		Context ctx = createContext();

		Integer output = handler.handleRequest(createInput("src/test/resources/payload.json"), ctx);

		CustomerDao dao=handler.getApplicationContext().getBean(CustomerDao.class);
		Customer customer=dao.get("id2", TABLE_NAME);
		// TODO: validate output here if needed.
		Assert.assertEquals("id2", customer.getId());
	}
}
