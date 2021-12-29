package com.aksh.kcl.enh.consumer.processor;

import com.aksh.avro.AvroSerDe;
import lombok.extern.slf4j.Slf4j;
import org.apache.avro.generic.GenericRecord;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.boot.json.GsonJsonParser;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.utils.StringInputStream;
import software.amazon.kinesis.exceptions.InvalidStateException;
import software.amazon.kinesis.exceptions.ShutdownException;
import software.amazon.kinesis.lifecycle.events.*;
import software.amazon.kinesis.processor.ShardRecordProcessor;
import software.amazon.kinesis.retrieval.KinesisClientRecord;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.util.Optional;

/**
 * Processes records and checkpoints progress.
 */
@Component
@Scope(scopeName = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
@Slf4j
public class SampleRecordProcessor implements ShardRecordProcessor {

	private static final String SHARD_ID_MDC_KEY = "ShardId";

	private String shardId;
	private final CharsetDecoder decoder = Charset.forName("UTF-8").newDecoder();

	AvroSerDe avroSerDe=new AvroSerDe();

	@Value("${glue.schema.enabled:false}")
	private boolean isGlueSchemaEnabled;


	public void initialize(InitializationInput initializationInput) {
		shardId = initializationInput.shardId();
		MDC.put(SHARD_ID_MDC_KEY, shardId);
		try {
			log.info("Initializing @ Sequence: {}", initializationInput.extendedSequenceNumber());
		} finally {
			MDC.remove(SHARD_ID_MDC_KEY);
		}
	}

	public void processRecords(ProcessRecordsInput processRecordsInput) {
		MDC.put(SHARD_ID_MDC_KEY, shardId);
		try {
			log.debug("Processing {} record(s)", processRecordsInput.records().size());
			processRecordsInput.records().forEach(this::processSingleRecord);
		} catch (Throwable t) {
			log.error("Caught throwable while processing records. Aborting.");
			Runtime.getRuntime().halt(1);
		} finally {
			MDC.remove(SHARD_ID_MDC_KEY);
		}
	}
	
	/**
     * Process a single record.
     * 
     * @param record The record to be processed.
     */
    private void processSingleRecord(KinesisClientRecord record) {
    	log.debug("Processing record pk: {} -- Seq: {}", record.partitionKey(), record.sequenceNumber());
        try {
			processAvroRecord(record);
			//processJsonRecord(record);
		} catch (Exception e) {
            log.info("Error in processing " , e);
        }
    }

	private void processAvroRecord(KinesisClientRecord record) throws Exception {
		ByteBuffer data=record.data();
		byte[] dataBytes;
    	if(record.data().isReadOnly()){
			ByteBuffer duplicateData = ByteBuffer.allocate(data.capacity());
			dataBytes = new byte[data.remaining()];
			data.get(dataBytes, 0, dataBytes.length);
		}else{
			dataBytes=record.data().array();
		}
		log.info("Data Receieved:" + new String(dataBytes));

    	if(isGlueSchemaEnabled){
			InputStream avroSchemIn=new StringInputStream(record.schema().getSchemaDefinition());
			GenericRecord genericRecord = avroSerDe.deserializeGeneric(avroSchemIn, dataBytes);
			log.info("Data After Parsing :" + genericRecord);

		}else{
			String avroSchema = "src/main/resources/avro/com/aksh/kcl/avro/fake/TradeData.avsc";
			GenericRecord genericRecord = avroSerDe.deserializeGeneric(new FileInputStream(avroSchema), dataBytes);
			log.info("Data After Parsing :" + genericRecord);
		}


	}

//	private GenericRecord recordToAvroObj(KinesisClientRecord r) {
//		byte[] data = new byte[r.data().remaining()];
//		r.data().get(data, 0, data.length);
//		org.apache.avro.Schema schema = new org.apache.avro.Schema.Parser().parse(r.schema().getSchemaDefinition());
//		DatumReader datumReader = new GenericDatumReader<>(schema);
//
//		BinaryDecoder binaryDecoder = DecoderFactory.get().binaryDecoder(data, 0, data.length, null);
//		return (GenericRecord) datumReader.read(null, binaryDecoder);
//	}

	private void processJsonRecord(KinesisClientRecord record)  {
		String data = null;
    	try{
			// For this app, we interpret the payload as UTF-8 chars.
			data = decoder.decode(record.data()).toString();
			// Assume this record came from AmazonKinesisSample and log its age.
			long recordCreateTime = getRecordTime(data);
			long ageOfRecordInMillis = System.currentTimeMillis() - recordCreateTime;

			log.info(record.sequenceNumber() + ", " + record.partitionKey() + ", " + data + ", Created "
					+ ageOfRecordInMillis + " milliseconds ago.");

		}catch (CharacterCodingException e) {
			log.error("Malformed data: " + data, e);
		}catch (NumberFormatException e) {
			log.info("Record does not match sample record format. Ignoring record with data; " + data);
		}
	}

	private long getRecordTime(String data) {
		long recordCreateTime=System.currentTimeMillis()+100000;
		try {
			String time = Optional.ofNullable(new GsonJsonParser().parseMap(data).get("time"))
					.orElse(data.substring("testData-".length())).toString();
			recordCreateTime= org.apache.commons.lang3.math.NumberUtils.toLong(time);
		} catch (Exception e) {
			log.debug("Error processing "+data,e);
		}
		return recordCreateTime;
	}

	public void leaseLost(LeaseLostInput leaseLostInput) {
		MDC.put(SHARD_ID_MDC_KEY, shardId);
		try {
			log.info("Lost lease, so terminating.");
		} finally {
			MDC.remove(SHARD_ID_MDC_KEY);
		}
	}

	public void shardEnded(ShardEndedInput shardEndedInput) {
		MDC.put(SHARD_ID_MDC_KEY, shardId);
		try {
			log.info("Reached shard end checkpointing.");
			shardEndedInput.checkpointer().checkpoint();
		} catch (ShutdownException | InvalidStateException e) {
			log.error("Exception while checkpointing at shard end. Giving up.", e);
		} finally {
			MDC.remove(SHARD_ID_MDC_KEY);
		}
	}

	public void shutdownRequested(ShutdownRequestedInput shutdownRequestedInput) {
		MDC.put(SHARD_ID_MDC_KEY, shardId);
		try {
			log.info("Scheduler is shutting down, checkpointing.");
			shutdownRequestedInput.checkpointer().checkpoint();
		} catch (ShutdownException | InvalidStateException e) {
			log.error("Exception while checkpointing at requested shutdown. Giving up.", e);
		} finally {
			MDC.remove(SHARD_ID_MDC_KEY);
		}
	}
}