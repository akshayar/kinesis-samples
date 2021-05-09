package com.aksh.kinesis.producer;

import java.util.concurrent.Executors;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import software.amazon.awssdk.core.SdkBytes;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.kinesis.KinesisAsyncClient;
import software.amazon.awssdk.services.kinesis.model.PutRecordRequest;
import software.amazon.kinesis.common.KinesisClientUtil;

@Component
class KineisAPI {
	
	
	@Autowired
	private Region region;
	
	@Value("${streamName:aksh-first}")
	String streamName = "aksh-first";
	
	String partitionPrefix = "partitionPrefix";
	
	KinesisAsyncClient client = KinesisClientUtil.createKinesisAsyncClient(KinesisAsyncClient.builder().region(region));
	

	private String createPayload() {
		return "testData-" + System.currentTimeMillis();
	}

	@PostConstruct
	void test() throws InterruptedException {

		Executors.newCachedThreadPool().execute(()->{
			int i = 0;

			while (true) {
				String payload=createPayload();
				System.out.println("Pushing Record "+payload);
				PutRecordRequest req = PutRecordRequest.builder().streamName(streamName)
						.data(SdkBytes.fromUtf8String(payload)).partitionKey(partitionPrefix + i % 4).build();
				client.putRecord(req);
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				i++;
			}
		});
		
		

	}

}
