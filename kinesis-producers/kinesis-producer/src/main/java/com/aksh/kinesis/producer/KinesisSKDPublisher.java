package com.aksh.kinesis.producer;

import software.amazon.awssdk.core.SdkBytes;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.kinesis.KinesisAsyncClient;
import software.amazon.awssdk.services.kinesis.model.PutRecordRequest;
import software.amazon.kinesis.common.KinesisClientUtil;

import java.nio.ByteBuffer;
import java.util.concurrent.Executors;
import java.util.function.Supplier;

public class KinesisSKDPublisher {
    private Region region;
    private boolean aggregationEnabled=false;
    private String streamName;
    private String partitionPrefix;

    public KinesisSKDPublisher(Region region, boolean aggregationEnabled, String streamName, String partitionPrefix) {
        this.region = region;
        this.aggregationEnabled = aggregationEnabled;
        this.streamName=streamName;
        this.partitionPrefix=partitionPrefix;
    }

    public void publish(Supplier<ByteBuffer> dataSupplier) throws Exception {
        KinesisAsyncClient client = KinesisClientUtil
                .createKinesisAsyncClient(KinesisAsyncClient.builder().region(region));

        Executors.newCachedThreadPool().execute(() -> {
            int i = 0;

            while (true) {
                String payload;
                try {
                    PutRecordRequest req = PutRecordRequest.builder().streamName(streamName)
                            .data(SdkBytes.fromByteBuffer(dataSupplier.get())).partitionKey(partitionPrefix + i % 2).build();
                    client.putRecord(req);

                    i++;
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
        });
    }

}
