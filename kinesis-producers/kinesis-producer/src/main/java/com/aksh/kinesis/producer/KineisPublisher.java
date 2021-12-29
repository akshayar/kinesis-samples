package com.aksh.kinesis.producer;

import com.google.gson.Gson;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.regions.Region;

import javax.annotation.PostConstruct;
import java.nio.ByteBuffer;
import java.util.Optional;

@Component
class KineisPublisher {

	@Autowired
	private Region region;

	@Value("${streamName:aksh-first}")
	String streamName = "aksh-first";

	@Value("${publishType:api}")
	String type = "api";

	String partitionPrefix = "partitionPrefix";

	
	Gson gson=new Gson();
	
	@Value("${intervalMs:100}")
	int intervalMs=100;

	@Autowired
	AvroByteArrayFromRandomObject byteArrayGenerationStrategy;

	
	@Value("${aggregationEnabled:false}")
	private boolean aggregationEnabled;

	

	@PostConstruct
	void pubish() throws Exception {

		if (Optional.ofNullable(type).orElse("api").equalsIgnoreCase("api")) {


			new KinesisSKDPublisher(region,aggregationEnabled,streamName,partitionPrefix).publish(()->{
				ByteBuffer data = null;
				try {
					data = byteArrayGenerationStrategy.generateData();
					sleep();
				} catch (Exception e) {
					e.printStackTrace();
				}
				return data;
			});
		} else {
			new KinesisKPLPublisher(region,aggregationEnabled,streamName,partitionPrefix).publish(()->{
				ByteBuffer data = null;
				try {
					data = byteArrayGenerationStrategy.generateData();
					sleep();
				} catch (Exception e) {
					e.printStackTrace();
				}
				return data;
			});
		}

	}

//	private void publishKPL() throws Exception {
//		// KinesisProducer gets credentials automatically like
//		// DefaultAWSCredentialsProviderChain.
//		// It also gets region automatically from the EC2 metadata service.
//		KinesisProducerConfiguration config = new KinesisProducerConfiguration().setAggregationEnabled(aggregationEnabled)
//				.setRecordMaxBufferedTime(3000).setMaxConnections(1).setRequestTimeout(60000);
//		config.setRegion(region.id());
//		KinesisProducer kinesis = new KinesisProducer(config);
//
//		// Put some records
//		int i = 0;
//		while (true) {
//			ByteBuffer data =byteArrayGenerationStrategy.generateData();
//			// doesn't block
//			kinesis.addUserRecord(streamName, partitionPrefix + i % 4, data);
//			sleep();
//			i++;
//		}
//	}



//	private void publishAPI() {
//		KinesisAsyncClient client = KinesisClientUtil
//				.createKinesisAsyncClient(KinesisAsyncClient.builder().region(region));
//
//		Executors.newCachedThreadPool().execute(() -> {
//			int i = 0;
//
//			while (true) {
//				String payload;
//				try {
//					ByteBuffer data = byteArrayGenerationStrategy.generateData();
//					sleep();
//					PutRecordRequest req = PutRecordRequest.builder().streamName(streamName)
//							.data(SdkBytes.fromByteBuffer(data)).partitionKey(partitionPrefix + i % 2).build();
//					client.putRecord(req);
//
//					i++;
//				} catch (Exception e) {
//					e.printStackTrace();
//				}
//
//			}
//		});
//	}

	private void sleep() {
		try {
			Thread.sleep(intervalMs);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

}
