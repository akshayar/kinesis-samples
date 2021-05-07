package com.aksh.kcl.enh.consumer;

import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.aksh.kcl.enh.consumer.processor.RecordProcessorFactory;

import software.amazon.awssdk.services.cloudwatch.CloudWatchAsyncClient;
import software.amazon.awssdk.services.dynamodb.DynamoDbAsyncClient;
import software.amazon.awssdk.services.kinesis.KinesisAsyncClient;
import software.amazon.kinesis.common.ConfigsBuilder;
import software.amazon.kinesis.coordinator.Scheduler;

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
	private KinesisAsyncClient kinesisClient;
	
	@Autowired
	private RecordProcessorFactory recordProcessorFactory;
	
	@Autowired
	private DynamoDbAsyncClient dynamoClient;
	
	@Autowired
	private CloudWatchAsyncClient cloudWatchClient;
	
	private Scheduler scheduler;

	@PostConstruct
	private void run() {

		ConfigsBuilder configsBuilder = new ConfigsBuilder(streamName, applicationName, kinesisClient, dynamoClient,
				cloudWatchClient, applicationName+UUID.randomUUID().toString(), recordProcessorFactory);

		scheduler= new Scheduler(configsBuilder.checkpointConfig(), configsBuilder.coordinatorConfig(),
				configsBuilder.leaseManagementConfig(), configsBuilder.lifecycleConfig(),
				configsBuilder.metricsConfig(), configsBuilder.processorConfig(), configsBuilder.retrievalConfig());

		Thread schedulerThread = new Thread(scheduler);
		schedulerThread.setDaemon(true);
		schedulerThread.start();
	}

	@PreDestroy
	public void shutDown() {
		log.info("Cancelling producer, and shutting down executor.");

		Future<Boolean> gracefulShutdownFuture = scheduler.startGracefulShutdown();
		log.info("Waiting up to {} seconds for shutdown to complete.",shutdownWaitSeconds);
		try {
			gracefulShutdownFuture.get(shutdownWaitSeconds, TimeUnit.SECONDS);
		} catch (InterruptedException e) {
			log.error("Interrupted while waiting for graceful shutdown. Continuing.",e);
		} catch (ExecutionException e) {
			log.error("Exception while executing graceful shutdown.", e);
		} catch (TimeoutException e) {
			log.error("Timeout while waiting for shutdown. Scheduler may not have exited.",e);
		}
		log.info("Completed, shutting down now.");
	}

}