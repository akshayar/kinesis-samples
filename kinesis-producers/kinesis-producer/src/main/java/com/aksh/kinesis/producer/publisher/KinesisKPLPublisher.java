package com.aksh.kinesis.producer.publisher;

import com.amazonaws.services.kinesis.producer.KinesisProducer;
import com.amazonaws.services.kinesis.producer.KinesisProducerConfiguration;
import com.amazonaws.services.schemaregistry.common.configs.GlueSchemaRegistryConfiguration;
import com.amazonaws.services.schemaregistry.utils.AWSSchemaRegistryConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.FileCopyUtils;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.glue.model.DataFormat;

import java.io.FileReader;
import java.nio.ByteBuffer;
import java.util.function.Supplier;

@Component
public class KinesisKPLPublisher {
    @Autowired
    private Region region;

    @Value("${KPL.aggregationEnabled:false}")
    private boolean aggregationEnabled;

    @Value("${streamName:aksh-first}")
    String streamName = "aksh-first";

    String partitionPrefix = "partitionPrefix";

    @Value("${AVRO.glue.schema.enabled:false}")
    private boolean isGlueSchemaRegisteryEnabled;

    @Value("${AVRO.glue.schema.name:demoSchema}")
    private String glueSchemaName;

    public KinesisKPLPublisher() {
    }

    public KinesisKPLPublisher(Region region, boolean aggregationEnabled, String streamName, String partitionPrefix, boolean isGlueSchemaRegisteryEnabled) {
        this.region = region;
        this.aggregationEnabled = aggregationEnabled;
        this.streamName = streamName;
        this.partitionPrefix = partitionPrefix;
        this.isGlueSchemaRegisteryEnabled = isGlueSchemaRegisteryEnabled;
    }

    public void publish(Supplier<ByteBuffer> dataSupplier) throws Exception {
        // KinesisProducer gets credentials automatically like
        // DefaultAWSCredentialsProviderChain.
        // It also gets region automatically from the EC2 metadata service.
        KinesisProducerConfiguration config = new KinesisProducerConfiguration().setAggregationEnabled(aggregationEnabled)
                .setRecordMaxBufferedTime(3000).setMaxConnections(1).setRequestTimeout(60000);
        config.setRegion(region.id());
        com.amazonaws.services.schemaregistry.common.Schema gsrSchema = null;
        if (isGlueSchemaRegisteryEnabled) {
            System.out.println("Glue Scheme Enabled");
            //Glue Schema Registery Code
            GlueSchemaRegistryConfiguration schemaRegistryConfig = new GlueSchemaRegistryConfiguration(region.id());
            schemaRegistryConfig.setCompressionType(AWSSchemaRegistryConstants.COMPRESSION.NONE);
            schemaRegistryConfig.setSchemaAutoRegistrationEnabled(true);


            config.setGlueSchemaRegistryConfiguration(schemaRegistryConfig);
            String schemaDefinition = FileCopyUtils.copyToString(new FileReader("src/main/resources/avro/com/aksh/kafka/avro/fake/TradeData.avsc"));
            gsrSchema =
                    new com.amazonaws.services.schemaregistry.common.Schema(schemaDefinition, DataFormat.AVRO.toString(), glueSchemaName);

        }

        KinesisProducer kinesis = new KinesisProducer(config);

        // Put some records
        int i = 0;
        while (true) {
            // doesn't block
            ByteBuffer data = dataSupplier.get();
            String hashKey = System.currentTimeMillis() + "";
            String partitionKey = partitionPrefix + i % 4;
            if (isGlueSchemaRegisteryEnabled) {
                kinesis.addUserRecord(streamName, partitionKey, hashKey, dataSupplier.get(), gsrSchema);

            } else {
                kinesis.addUserRecord(streamName, partitionKey, dataSupplier.get());
            }
            i++;
        }
    }

}
