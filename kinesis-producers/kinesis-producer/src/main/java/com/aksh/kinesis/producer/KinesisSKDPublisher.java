package com.aksh.kinesis.producer;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import software.amazon.awssdk.core.SdkBytes;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.kinesis.KinesisAsyncClient;
import software.amazon.awssdk.services.kinesis.model.PutRecordRequest;
import software.amazon.kinesis.common.KinesisClientUtil;

import java.nio.ByteBuffer;
import java.util.concurrent.Executors;
import java.util.function.Supplier;

public class KinesisSKDPublisher {

    @Autowired
    private Region region;

    @Value("${streamName:aksh-first}")
    String streamName = "aksh-first";

    String partitionPrefix = "partitionPrefix";

    public KinesisSKDPublisher() {
    }

    public KinesisSKDPublisher(Region region, String streamName, String partitionPrefix) {
        this.region = region;
        this.streamName = streamName;
        this.partitionPrefix = partitionPrefix;
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
