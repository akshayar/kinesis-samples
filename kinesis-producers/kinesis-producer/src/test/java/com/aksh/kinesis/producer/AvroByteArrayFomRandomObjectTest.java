package com.aksh.kinesis.producer;

import com.aksh.kinesis.producer.avro.AvroSerDe;
import com.aksh.kinesis.producer.faker.JSRandomDataGenerator;
import com.aksh.kinesis.producer.publisher.AvroByteArrayFromRandomObjectGenStrategy;
import org.junit.jupiter.api.Test;

import java.io.FileInputStream;
import java.nio.ByteBuffer;
import java.util.Properties;

public class AvroByteArrayFomRandomObjectTest {
    AvroByteArrayFromRandomObjectGenStrategy avroByteArrayFromRandomObjectGenStrategy =new AvroByteArrayFromRandomObjectGenStrategy(new JSRandomDataGenerator());
    AvroSerDe avroSerDe=new AvroSerDe();
    @Test
    public void serialize() throws  Exception{
        avroByteArrayFromRandomObjectGenStrategy.setAvroSchemaPath("src/main/resources/avro/com/aksh/kafka/avro/fake/TradeData.avsc");
        avroByteArrayFromRandomObjectGenStrategy.setFakeGeneratorJSScript("src/main/resources/generate-data.js");
        ByteBuffer bufffer= avroByteArrayFromRandomObjectGenStrategy.generateData();


        Properties dataOut= avroSerDe.deserializeToProperties(new FileInputStream("src/main/resources/avro/com/aksh/kafka/avro/fake/TradeData.avsc"),bufffer);
        System.out.println("Object output properties:"+dataOut);


    }

    @Test
    public void deserialize() {
    }
}