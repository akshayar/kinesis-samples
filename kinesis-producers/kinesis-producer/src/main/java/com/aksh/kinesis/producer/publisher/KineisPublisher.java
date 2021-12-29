package com.aksh.kinesis.producer.publisher;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.nio.ByteBuffer;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Supplier;

@Component
class KineisPublisher implements ApplicationContextAware {


	@Value("${publishType:api}")
	String type = "api";
	
	@Value("${intervalMs:100}")
	int intervalMs=100;

	@Value("${DataGenerationStrategy.type}")
	String dataGenerationStrategyType="AVRO";

	DataGenerationStrategy dataGenerationStrategy;

	private ApplicationContext applicationContext;


	@Override
	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
		this.applicationContext=applicationContext;
	}

	@PostConstruct
	public void init() throws Exception{
		if("AVRO".equalsIgnoreCase(dataGenerationStrategyType)){
			dataGenerationStrategy=applicationContext.getBean(AvroDataFromRandomObjectGenStrategy.class);
		}else{
			dataGenerationStrategy=applicationContext.getBean(JsonDataFromRandomObjectGenStrategy.class);
		}
		ExecutorService executorService=Executors.newCachedThreadPool();
		CompletableFuture future= CompletableFuture.supplyAsync(()->{
			try {
				this.publish();
				return "SUCCESS";
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		},executorService).exceptionally(ex->{
			ex.printStackTrace();
			return "FAILURE";
		});
		executorService.shutdown();
	}



	void publish() throws Exception {
		Supplier<ByteBuffer> dataSupplier=()->{
			ByteBuffer data = null;
			try {
				data = dataGenerationStrategy.generateData();
				sleep();
			} catch (Exception e) {
				e.printStackTrace();
				throw new RuntimeException(e);
			}
			return data;
		};

		if (Optional.ofNullable(type).orElse("api").equalsIgnoreCase("api")) {
			applicationContext.getBean(KinesisSKDPublisher.class).publish(dataSupplier);
		} else {
			applicationContext.getBean(KinesisKPLPublisher.class).publish(dataSupplier);
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
