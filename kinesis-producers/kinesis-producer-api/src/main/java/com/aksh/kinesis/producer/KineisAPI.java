package com.aksh.kinesis.producer;



import javax.annotation.PostConstruct;

import software.amazon.awssdk.core.SdkBytes;
import software.amazon.awssdk.services.kinesis.KinesisAsyncClient;
import software.amazon.awssdk.services.kinesis.KinesisAsyncClientBuilder;
import software.amazon.awssdk.services.kinesis.model.PutRecordRequest;

class KineisAPI {
	KinesisAsyncClient client=new Kinesi
	@PostConstruct
	void test() throws InterruptedException {
		String partition="partitionId";
		int i=0;
		
		while(true) {
			PutRecordRequest req=PutRecordRequest.builder().streamName("aksh-first")
					.data(SdkBytes.fromUtf8String("testData-" + System.currentTimeMillis())).partitionKey(partition+i%4)
					.build();
			KinesisClientUtil.createKinesisAsyncClient(KinesisAsyncClient.builder()).putRecord(req);
			Thread.sleep(1000);
			i++;
		}
		
		
	}

}
