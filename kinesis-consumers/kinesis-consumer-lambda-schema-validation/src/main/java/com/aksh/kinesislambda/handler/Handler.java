/**
 * 
 */
package com.aksh.kinesislambda.handler;

import com.amazonaws.services.kinesis.clientlibrary.types.UserRecord;
import com.amazonaws.services.lambda.runtime.events.KinesisFirehoseEvent;
import com.amazonaws.services.schemaregistry.common.GlueSchemaRegistryDataFormatDeserializer;
import com.amazonaws.services.schemaregistry.common.configs.GlueSchemaRegistryConfiguration;
import com.amazonaws.services.schemaregistry.deserializers.GlueSchemaRegistryDeserializer;
import com.amazonaws.services.schemaregistry.deserializers.GlueSchemaRegistryDeserializerFactory;
import com.amazonaws.services.schemaregistry.deserializers.GlueSchemaRegistryDeserializerImpl;
import com.amazonaws.services.schemaregistry.deserializers.json.JsonDeserializer;
import com.amazonaws.services.schemaregistry.serializers.GlueSchemaRegistrySerializer;
import com.amazonaws.services.schemaregistry.serializers.GlueSchemaRegistrySerializerImpl;
import com.amazonaws.services.schemaregistry.serializers.json.JsonSerializer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.http.urlconnection.UrlConnectionHttpClient;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.glue.GlueClient;
import software.amazon.awssdk.services.glue.GlueClientBuilder;
import software.amazon.awssdk.services.glue.model.*;

import javax.annotation.PostConstruct;
import java.util.Optional;

/**
 * @author aksrawat
 *
 */
@Component
@Slf4j
public class Handler {

	//Configurations for Schema Registry
	GlueSchemaRegistryConfiguration gsrConfig = new GlueSchemaRegistryConfiguration("ap-south-1");
	AwsCredentialsProvider awsCredentialsProvider =
			DefaultCredentialsProvider
					.builder()
					.build();



	String REGISTRY_NAME_DEFAULT="default-registry";
	String SCHEMA_NAME = "theTargetSchema";

	String registryName;
	String schemaName;

	String schemaDefinition;
	JsonSerializer jsonSerializer;


	@PostConstruct
	public void init() {
		registryName=Optional.ofNullable(System.getenv("registryName")).orElse(REGISTRY_NAME_DEFAULT);
		schemaName=Optional.ofNullable(System.getenv("schemaName")).orElse(SCHEMA_NAME);

		gsrConfig.setRegistryName(registryName);
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
		schemaDefinition=getSchemaVersionResponse.schemaDefinition();

		jsonSerializer=new JsonSerializer(gsrConfig);



	}


	public boolean validateKinesisUserRecord(UserRecord record) {
		boolean processed=true;
		try {
			String payload = new String(record.getData().array());
			jsonSerializer.validate(schemaDefinition,payload.getBytes());

		}catch (Exception e){
			log.info("Schema:"+schemaDefinition);
			log.info("Payload: " + new String(record.getData().array()));
			processed=false;
			log.error("Error in processing",e);
		}
		return processed;

	}

	public boolean validateFirehoseRecord(KinesisFirehoseEvent.Record record){
		boolean processed=true;
		try {
			String payload = new String(record.getData().array());
			jsonSerializer.validate(schemaDefinition,payload.getBytes());

		}catch (Exception e){
			log.info("Schema:"+schemaDefinition);
			log.info("Payload: " + new String(record.getData().array()));
			processed=false;
			log.error("Error in processing",e);
		}
		return processed;

	}

}
