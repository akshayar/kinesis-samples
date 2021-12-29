package com.aksh.kcl.enh.consumer;

import com.aksh.kcl.enh.consumer.processor.RecordProcessorFactory;
import com.amazonaws.services.schemaregistry.common.configs.GlueSchemaRegistryConfiguration;
import com.amazonaws.services.schemaregistry.deserializers.GlueSchemaRegistryDeserializer;
import com.amazonaws.services.schemaregistry.deserializers.GlueSchemaRegistryDeserializerImpl;
import com.amazonaws.services.schemaregistry.utils.AWSSchemaRegistryConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.cloudwatch.CloudWatchAsyncClient;
import software.amazon.awssdk.services.dynamodb.DynamoDbAsyncClient;
import software.amazon.awssdk.services.kinesis.KinesisAsyncClient;
import software.amazon.kinesis.common.ConfigsBuilder;
import software.amazon.kinesis.coordinator.Scheduler;
import software.amazon.kinesis.retrieval.RetrievalConfig;
import software.amazon.kinesis.retrieval.polling.PollingConfig;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

@Component
public class ApplicationBootStrapper {

    private static final Logger log = LoggerFactory.getLogger(ApplicationBootStrapper.class);

    @Value("${streamName:aksh-first}")
    public String streamName;

    @Value("${applicationName:SampleKinesisApplication}")
    private String applicationName;

    @Value("${shutdownWaitSeconds:60}")
    private int shutdownWaitSeconds;

    @Autowired
    private Region region;

    @Autowired
    private KinesisAsyncClient kinesisClient;

    @Autowired
    private RecordProcessorFactory recordProcessorFactory;

    @Autowired
    private DynamoDbAsyncClient dynamoClient;

    @Autowired
    private CloudWatchAsyncClient cloudWatchClient;

    private Scheduler scheduler;

    @Value("${glue.schema.enabled:false}")
    private boolean isGlueSchemaEnabled;

    @PostConstruct
    private void run() {
        log.info("Stream Name=" + streamName);

        ConfigsBuilder configsBuilder = new ConfigsBuilder(streamName, applicationName, kinesisClient, dynamoClient,
                cloudWatchClient, applicationName + UUID.randomUUID().toString(), recordProcessorFactory);

        RetrievalConfig retrievalConfig = configsBuilder.retrievalConfig().retrievalSpecificConfig(new PollingConfig(streamName, kinesisClient));

        if(isGlueSchemaEnabled){
            GlueSchemaRegistryConfiguration schemaRegistryConfig = new GlueSchemaRegistryConfiguration(region.id());
            schemaRegistryConfig.setCompressionType(AWSSchemaRegistryConstants.COMPRESSION.NONE);
            schemaRegistryConfig.setSchemaAutoRegistrationEnabled(true);

            GlueSchemaRegistryDeserializer glueSchemaRegistryDeserializer =
                    new GlueSchemaRegistryDeserializerImpl(DefaultCredentialsProvider.builder().build(), schemaRegistryConfig);

            retrievalConfig.glueSchemaRegistryDeserializer(glueSchemaRegistryDeserializer);
        }



        scheduler = new Scheduler(configsBuilder.checkpointConfig(), configsBuilder.coordinatorConfig(),
                configsBuilder.leaseManagementConfig(), configsBuilder.lifecycleConfig(),
                configsBuilder.metricsConfig(), configsBuilder.processorConfig(), retrievalConfig);

        Thread schedulerThread = new Thread(scheduler);
        schedulerThread.setDaemon(true);
        schedulerThread.start();
    }

    @PreDestroy
    public void shutDown() {
        log.info("Cancelling producer, and shutting down executor.");

        Future<Boolean> gracefulShutdownFuture = scheduler.startGracefulShutdown();
        log.info("Waiting up to {} seconds for shutdown to complete.", shutdownWaitSeconds);
        try {
            gracefulShutdownFuture.get(shutdownWaitSeconds, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            log.error("Interrupted while waiting for graceful shutdown. Continuing.", e);
        } catch (ExecutionException e) {
            log.error("Exception while executing graceful shutdown.", e);
        } catch (TimeoutException e) {
            log.error("Timeout while waiting for shutdown. Scheduler may not have exited.", e);
        }
        log.info("Completed, shutting down now.");
    }

}