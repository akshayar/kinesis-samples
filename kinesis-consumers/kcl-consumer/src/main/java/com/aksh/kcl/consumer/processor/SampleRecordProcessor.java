package com.aksh.kcl.consumer.processor;

import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.util.List;
import java.util.Optional;
import java.util.Random;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.boot.json.GsonJsonParser;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.amazonaws.services.kinesis.clientlibrary.exceptions.InvalidStateException;
import com.amazonaws.services.kinesis.clientlibrary.exceptions.ShutdownException;
import com.amazonaws.services.kinesis.clientlibrary.exceptions.ThrottlingException;
import com.amazonaws.services.kinesis.clientlibrary.interfaces.IRecordProcessor;
import com.amazonaws.services.kinesis.clientlibrary.interfaces.IRecordProcessorCheckpointer;
import com.amazonaws.services.kinesis.clientlibrary.lib.worker.ShutdownReason;
import com.amazonaws.services.kinesis.model.Record;
import com.google.gson.Gson;

import lombok.extern.slf4j.Slf4j;

/**
 * Processes records and checkpoints progress.
 */
@Component
@Scope(scopeName = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
@Slf4j
public class SampleRecordProcessor implements IRecordProcessor {

    private String kinesisShardId;

    // Backoff and retry settings
    private static final long BACKOFF_TIME_IN_MILLIS = 3000L;
    private static final int NUM_RETRIES = 10;

    // Checkpoint about once a minute
    private static final long CHECKPOINT_INTERVAL_MILLIS = 60000L;
    private long nextCheckpointTimeInMillis;

    private final CharsetDecoder decoder = Charset.forName("UTF-8").newDecoder();
   
    @Autowired
    private TradeDao tradeDao;
    private Gson gson=new Gson();
    
    Random random=new Random();

    /**
     * {@inheritDoc}
     */
    @Override
    public void initialize(String shardId) {
        log.info("Initializing record processor for shard: " + shardId);
        this.kinesisShardId = shardId;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void processRecords(List<Record> records, IRecordProcessorCheckpointer checkpointer) {
        log.debug("Processing " + records.size() + " records from " + kinesisShardId);

        // Process records and perform all exception handling.
        processRecordsWithRetries(records);

        // Checkpoint once every checkpoint interval.
        if (System.currentTimeMillis() > nextCheckpointTimeInMillis) {
            checkpoint(checkpointer);
            nextCheckpointTimeInMillis = System.currentTimeMillis() + CHECKPOINT_INTERVAL_MILLIS;
        }
    }

    /**
     * Process records performing retries as needed. Skip "poison pill" records.
     * 
     * @param records Data records to be processed.
     */
    private void processRecordsWithRetries(List<Record> records) {
        for (Record record : records) {
            boolean processedSuccessfully = false;
            for (int i = 0; i < NUM_RETRIES; i++) {
                try {
                    //
                    // Logic to process record goes here.
                    //
                	Thread.sleep(random.nextInt(10000));
                    processSingleRecord(record);

                    processedSuccessfully = true;
                    break;
                } catch (Throwable t) {
                    log.warn("Caught throwable while processing record " + record, t);
                }

                // backoff if we encounter an exception.
                try {
                    Thread.sleep(BACKOFF_TIME_IN_MILLIS);
                } catch (InterruptedException e) {
                    log.debug("Interrupted sleep", e);
                }
            }

            if (!processedSuccessfully) {
                log.error("Couldn't process record " + record + ". Skipping the record.");
            }
        }
    }

    /**
     * Process a single record.
     * 
     * @param record The record to be processed.
     */
    private void processSingleRecord(Record record) {
        // TODO Add your own record processing logic here

        String data = null;
        try {
            // For this app, we interpret the payload as UTF-8 chars.
            data = decoder.decode(record.getData()).toString();
            log.info("Data Receieved:"+data);
            // Assume this record came from AmazonKinesisSample and log its age.
            Trade trade=gson.fromJson(data, Trade.class);
            if(Optional.ofNullable(trade).isPresent()) {
            	long recordCreateTime = trade.getTimestamp();
    			long ageOfRecordInMillis = System.currentTimeMillis() - recordCreateTime;
    			tradeDao.save(trade);

                log.info(record.getSequenceNumber() + ", " + record.getPartitionKey() + ", " + data + ", Created "
                        + ageOfRecordInMillis + " milliseconds ago.");
            }else {
            	 log.info("Nulll Record");
            }
			
        } catch (NumberFormatException e) {
            log.info("Record does not match sample record format. Ignoring record with data; " + data);
        } catch (CharacterCodingException e) {
            log.error("Malformed data: " + data, e);
        }
    }

	private long getRecordTime(String data) {
		long recordCreateTime=System.currentTimeMillis()+100000;
		try {
			String time = Optional.ofNullable(new GsonJsonParser().parseMap(data).get("time"))
					.orElse(data.substring("testData-".length())).toString();
			recordCreateTime= org.apache.commons.lang3.math.NumberUtils.toLong(time);
		} catch (Exception e) {
			log.error("Error processing "+data,e);
		}
		return recordCreateTime;
	}

    /**
     * {@inheritDoc}
     */
    @Override
    public void shutdown(IRecordProcessorCheckpointer checkpointer, ShutdownReason reason) {
        log.info("Shutting down record processor for shard: " + kinesisShardId);
        // Important to checkpoint after reaching end of shard, so we can start processing data from child shards.
        if (reason == ShutdownReason.TERMINATE) {
            checkpoint(checkpointer);
        }
    }

    /** Checkpoint with retries.
     * @param checkpointer
     */
    private void checkpoint(IRecordProcessorCheckpointer checkpointer) {
        log.info("Checkpointing shard " + kinesisShardId);
        for (int i = 0; i < NUM_RETRIES; i++) {
            try {
                checkpointer.checkpoint();
                break;
            } catch (ShutdownException se) {
                // Ignore checkpoint if the processor instance has been shutdown (fail over).
                log.info("Caught shutdown exception, skipping checkpoint.", se);
                break;
            } catch (ThrottlingException e) {
                // Backoff and re-attempt checkpoint upon transient failures
                if (i >= (NUM_RETRIES - 1)) {
                    log.error("Checkpoint failed after " + (i + 1) + "attempts.", e);
                    break;
                } else {
                    log.info("Transient issue when checkpointing - attempt " + (i + 1) + " of "
                            + NUM_RETRIES, e);
                }
            } catch (InvalidStateException e) {
                // This indicates an issue with the DynamoDB table (check for table, provisioned IOPS).
                log.error("Cannot save checkpoint to the DynamoDB table used by the Amazon Kinesis Client Library.", e);
                break;
            }
            try {
                Thread.sleep(BACKOFF_TIME_IN_MILLIS);
            } catch (InterruptedException e) {
                log.debug("Interrupted sleep", e);
            }
        }
    }
}