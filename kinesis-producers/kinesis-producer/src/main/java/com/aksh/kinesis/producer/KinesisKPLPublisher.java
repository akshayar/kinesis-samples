package com.aksh.kinesis.producer;

import com.amazonaws.services.kinesis.producer.KinesisProducer;
import com.amazonaws.services.kinesis.producer.KinesisProducerConfiguration;
import com.amazonaws.services.schemaregistry.common.configs.GlueSchemaRegistryConfiguration;
import com.amazonaws.services.schemaregistry.utils.AWSSchemaRegistryConstants;
import org.springframework.util.FileCopyUtils;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.glue.model.DataFormat;

import java.io.FileReader;
import java.nio.ByteBuffer;
import java.util.function.Supplier;

public class KinesisKPLPublisher {
    private Region region;
    private boolean aggregationEnabled=false;
    private String streamName;
    private String partitionPrefix;

    public KinesisKPLPublisher(Region region, boolean aggregationEnabled,String streamName,String partitionPrefix) {
        this.region = region;
        this.aggregationEnabled = aggregationEnabled;
        this.streamName=streamName;
        this.partitionPrefix=partitionPrefix;
    }

    public void publish(Supplier<ByteBuffer> dataSupplier) throws Exception {
        // KinesisProducer gets credentials automatically like
        // DefaultAWSCredentialsProviderChain.
        // It also gets region automatically from the EC2 metadata service.
        KinesisProducerConfiguration config = new KinesisProducerConfiguration().setAggregationEnabled(aggregationEnabled)
                .setRecordMaxBufferedTime(3000).setMaxConnections(1).setRequestTimeout(60000);


        GlueSchemaRegistryConfiguration schemaRegistryConfig =new GlueSchemaRegistryConfiguration(region.id());
        schemaRegistryConfig.setCompressionType(AWSSchemaRegistryConstants.COMPRESSION.NONE);
        schemaRegistryConfig.setSchemaAutoRegistrationEnabled(true);

        config.setRegion(region.id());
        config.setGlueSchemaRegistryConfiguration(schemaRegistryConfig);


        KinesisProducer kinesis = new KinesisProducer(config);
        String schemaDefinition= FileCopyUtils.copyToString(new FileReader("src/main/resources/avro/com/aksh/kafka/avro/fake/TradeData.avsc"));
        com.amazonaws.services.schemaregistry.common.Schema gsrSchema =
                new com.amazonaws.services.schemaregistry.common.Schema(schemaDefinition, DataFormat.AVRO.toString(), "demoSchema");

        // Put some records
        int i = 0;
        while (true) {
            // doesn't block
            ByteBuffer data=dataSupplier.get();
            String hashKey=System.currentTimeMillis()+"";
            String partitionKey=partitionPrefix + i % 4;

            kinesis.addUserRecord(streamName,partitionKey ,hashKey , dataSupplier.get(),gsrSchema);
            i++;
        }
    }

}
