package com.aksh.kpl.producer;

import com.amazonaws.services.kinesis.producer.KinesisProducer;
import com.amazonaws.services.kinesis.producer.KinesisProducerConfiguration;
import com.amazonaws.services.kinesis.producer.UserRecordResult;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.regions.Region;

import javax.annotation.PostConstruct;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

@Component
class KineisPublisher {

	private static final Logger logger= LoggerFactory.getLogger(KineisPublisher.class);

	@Autowired
	private Region region;

    @Value("${aggregationEnabled:false}")
    private boolean aggregationEnabled;
    @Value("${recordMaxBufferedTimeMs:3000}")
    private int recordMaxBufferedTimeMs;
    @Value("${maxConnections:1}")
    private int maxConnections;
    @Value("${requestTimeoutMs:60000}")
    private int requestTimeoutMs;

	private KinesisProducer kinesis;

	Executor exec= Executors.newCachedThreadPool();

    @PostConstruct
    void init() throws Exception {
        KinesisProducerConfiguration config = new KinesisProducerConfiguration()
				.setAggregationEnabled(aggregationEnabled)
                .setRecordMaxBufferedTime(recordMaxBufferedTimeMs)
				.setMaxConnections(maxConnections)
				.setRequestTimeout(requestTimeoutMs)
				.setRegion(region.id());
        kinesis = new KinesisProducer(config);

    }

    public void publishKPL(String payload,String streamName){
        // KinesisProducer gets credentials automatically like
        // DefaultAWSCredentialsProviderChain.
        // It also gets region automatically from the EC2 metadata service.
        FutureCallback<UserRecordResult> myCallback =new FutureCallback<UserRecordResult>() {
            @Override
            public void onSuccess(@Nullable UserRecordResult result) {
                logger.info("Shard id:{},sequence number:{}, status:{} ",result.getShardId(),result.getSequenceNumber(),result.isSuccessful());

            }

            @Override
            public void onFailure(Throwable t) {
                logger.error("Error Publishing Records",t);
            }
        };

		logger.info("Stream:{},KPL Push:{} ",streamName, payload);
        ByteBuffer data = ByteBuffer.wrap(payload.getBytes(StandardCharsets.UTF_8));
        String partitionKey=System.currentTimeMillis()+"";
        ListenableFuture<UserRecordResult> f =kinesis.addUserRecord(streamName, partitionKey, data);
        Futures.addCallback(f,myCallback,exec);

    }

}
