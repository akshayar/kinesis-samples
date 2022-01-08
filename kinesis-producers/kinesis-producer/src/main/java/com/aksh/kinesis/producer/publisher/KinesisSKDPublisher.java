package com.aksh.kinesis.producer.publisher;

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

/**
 * This class uses Kinesis SDK.
 * Not using it right now.
 * This can be integrated with Glue Schema registery , not done at present.
 * Refer https://docs.aws.amazon.com/glue/latest/dg/schema-registry-integrations.html#schema-registry-integrations-kds
 */
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
                    throw new RuntimeException(e);
                }

            }
        });
    }

}
