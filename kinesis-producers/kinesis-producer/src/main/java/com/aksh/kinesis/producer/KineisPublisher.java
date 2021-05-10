package com.aksh.kinesis.producer;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Optional;
import java.util.Random;
import java.util.concurrent.Executors;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.json.GsonJsonParser;
import org.springframework.stereotype.Component;

import com.amazonaws.services.kinesis.producer.KinesisProducer;
import com.amazonaws.services.kinesis.producer.KinesisProducerConfiguration;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.S3Object;
import com.google.gson.Gson;

import software.amazon.awssdk.core.SdkBytes;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.kinesis.KinesisAsyncClient;
import software.amazon.awssdk.services.kinesis.model.PutRecordRequest;
import software.amazon.awssdk.utils.IoUtils;
import software.amazon.kinesis.common.KinesisClientUtil;

@Component
class KineisPublisher {

	@Autowired
	private Region region;

	@Value("${streamName:aksh-first}")
	String streamName = "aksh-first";

	@Value("${publishType:api}")
	String type = "api";

	String partitionPrefix = "partitionPrefix";

	@Value("${bucketName:aksh-test-versioning}")
	String bucket = "aksh-test-versioning";

	AmazonS3 s3 = AmazonS3ClientBuilder.defaultClient();
	String template;
	
	Gson gson=new Gson();
	
	@Value("${intervalMs:100}")
	int intervalMs=100;

	private String createPayload() throws IOException {
		String payload= randomize(template);
		System.out.println("Pushing Record " + payload);
		return payload;
	}

	private String readTemplate() throws IOException {
		String template = "testData-" + System.currentTimeMillis();
		try {
			S3Object object = s3.getObject(bucket, "kinesis/payload/file.txt");
			template = IoUtils.toUtf8String(object.getObjectContent().getDelegateStream());
			String time = Optional.ofNullable(new GsonJsonParser().parseMap(template).get("time"))
					.orElse(template.substring("testData-".length())).toString();
			System.out.println("Time from template:"+template+", is:"+time);
		} catch (Exception e) {
			e.printStackTrace();
		} 
		
		return template;
	}

	private String randomize(String template) {
		return template.replaceAll("RANDOM10", generateRandomString(10)).replaceAll("RANDOM2", generateRandomString(2))
				.replaceAll("RANDOM5", generateRandomString(5)).replaceAll("EPOCH", System.currentTimeMillis() + "");

	}

	private String generateRandomString(int targetStringLength) {
		int leftLimit = 97; // letter 'a'
		int rightLimit = 122; // letter 'z'
		Random random = new Random();

		String generatedString = random.ints(leftLimit, rightLimit + 1).limit(targetStringLength)
				.collect(StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append).toString();

		return generatedString;
	}

	@PostConstruct
	void pubish() throws Exception {
		template = readTemplate();

		if (Optional.ofNullable(type).orElse("api").equalsIgnoreCase("api")) {
			publishAPI();
		} else {
			publishKPL();
		}

	}

	private void publishKPL() throws IOException {
		// KinesisProducer gets credentials automatically like
		// DefaultAWSCredentialsProviderChain.
		// It also gets region automatically from the EC2 metadata service.
		KinesisProducerConfiguration config=new KinesisProducerConfiguration();
		config.setRegion(region.id());
		config.setAggregationEnabled(true);
		KinesisProducer kinesis = new KinesisProducer(config);
		
		// Put some records
		int i = 0;
		while (true) {
			String payload = createPayload();
			ByteBuffer data = ByteBuffer.wrap(payload.getBytes("UTF-8"));
			// doesn't block
			kinesis.addUserRecord(streamName, partitionPrefix + i % 4, data);
			sleep();
			i++;
		}
	}

	private void publishAPI() {
		KinesisAsyncClient client = KinesisClientUtil
				.createKinesisAsyncClient(KinesisAsyncClient.builder().region(region));

		Executors.newCachedThreadPool().execute(() -> {
			int i = 0;

			while (true) {
				String payload;
				try {
					payload = createPayload();
					PutRecordRequest req = PutRecordRequest.builder().streamName(streamName)
							.data(SdkBytes.fromUtf8String(payload)).partitionKey(partitionPrefix + i % 4).build();
					client.putRecord(req);
					sleep();
					i++;
				} catch (IOException e) {
					e.printStackTrace();
				}
				
			}
		});
	}

	private void sleep() {
		try {
			Thread.sleep(intervalMs);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

}
