package com.aksh.kinesislambda;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.events.KinesisEvent;
import com.amazonaws.services.schemaregistry.common.Schema;
import com.amazonaws.services.schemaregistry.common.configs.GlueSchemaRegistryConfiguration;
import com.amazonaws.services.schemaregistry.serializers.GlueSchemaRegistrySerializer;
import com.amazonaws.services.schemaregistry.serializers.GlueSchemaRegistrySerializerImpl;
import com.amazonaws.util.StringInputStream;
import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.util.Base64Utils;
import org.springframework.util.FileCopyUtils;
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.http.urlconnection.UrlConnectionHttpClient;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.glue.GlueClient;
import software.amazon.awssdk.services.glue.GlueClientBuilder;
import software.amazon.awssdk.services.glue.model.*;

import java.io.FileReader;
import java.io.IOException;
import java.io.UncheckedIOException;

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
			String registryName="default-registry";
			String schemaName="testJSON";
			GlueSchemaRegistryConfiguration gsrConfig = new GlueSchemaRegistryConfiguration("ap-south-1");
			AwsCredentialsProvider awsCredentialsProvider =
					DefaultCredentialsProvider
							.builder()
							.build();
			GlueSchemaRegistrySerializer glueSchemaRegistrySerializer = new GlueSchemaRegistrySerializerImpl(awsCredentialsProvider, gsrConfig);
			GlueClientBuilder glueClientBuilder = GlueClient
					.builder()
					//.credentialsProvider(awsCredentialsProvider)
					.httpClient(UrlConnectionHttpClient.builder().build())
					.region(Region.of(gsrConfig.getRegion()));
			GlueClient glueClient=glueClientBuilder.build();
			SchemaId schemaId=SchemaId.builder()
					.registryName(registryName)
					.schemaName(schemaName)
					.build();
			SchemaVersionNumber schemaVersionNumber=SchemaVersionNumber.builder()
					.latestVersion(true)
					.build();
			GetSchemaVersionRequest getSchemaVersionRequest=GetSchemaVersionRequest.builder()
					.schemaId(schemaId)
					.schemaVersionNumber(schemaVersionNumber)
					.build();
			GetSchemaVersionResponse getSchemaVersionResponse=glueClient.getSchemaVersion(getSchemaVersionRequest);
			String schemaDefinition=getSchemaVersionResponse.schemaDefinition();

			Schema gsrSchema =
					new com.amazonaws.services.schemaregistry.common.Schema(schemaDefinition,DataFormat.JSON.toString(),schemaName);



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

//		CustomerDao dao=handler.getApplicationContext().getBean(CustomerDao.class);
//		Customer customer=dao.get("id2", TABLE_NAME);
//		// TODO: validate output here if needed.
//		Assert.assertEquals("id2", customer.getId());package
	}
}
