package com.aksh.kcl.consumer.processor;

import com.aksh.avro.AvroSerDe;
import com.amazonaws.services.kinesis.model.Record;
import com.google.gson.Gson;
import lombok.extern.slf4j.Slf4j;
import org.apache.avro.generic.GenericRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.json.GsonJsonParser;
import org.springframework.stereotype.Component;

import java.io.FileInputStream;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.util.Optional;
@Slf4j @Component
public class SingleRecordProcessor {


    private final CharsetDecoder decoder = Charset.forName("UTF-8").newDecoder();

    AvroSerDe avroSerDe=new AvroSerDe();

    @Autowired
    private TradeDao tradeDao;

    private Gson gson=new Gson();

    /**
     * Process a single record.
     *
     * @param record The record to be processed.
     */
    public void processSingleRecord(Record record) {
        String data = null;
        try {
            // For this app, we interpret the payload as UTF-8 chars.
            //data = decoder.decode(record.getData()).toString();
            log.info("Data Receieved:"+new String(record.getData().array()));
            String avroSchame="src/main/resources/avro/com/aksh/kcl/avro/fake/TradeData.avsc";
            GenericRecord genericRecord=avroSerDe.deserializeGeneric(new FileInputStream(avroSchame),record.getData());
            log.info("Data After Parsing :"+genericRecord);


        } catch (Exception e) {
            log.info("Record does not match sample record format. Ignoring record with data; " + data);
        }
//        catch (CharacterCodingException e) {
//            log.error("Malformed data: " + data, e);
//        }
    }

    private Trade saveTradeData(Record record, String data) {
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
        return trade;
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
}
