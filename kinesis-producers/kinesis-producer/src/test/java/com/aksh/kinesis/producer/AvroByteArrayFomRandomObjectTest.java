package com.aksh.kinesis.producer;

import com.aksh.kinesis.producer.fake.JSRandomDataGenerator;
import org.junit.jupiter.api.Test;

import java.io.FileInputStream;
import java.nio.ByteBuffer;
import java.util.Properties;

public class AvroByteArrayFomRandomObjectTest {
    AvroByteArrayFromRandomObject avroByteArrayFromRandomObject=new AvroByteArrayFromRandomObject(new JSRandomDataGenerator());
    AvroSerDe avroSerDe=new AvroSerDe();
    @Test
    public void serialize() throws  Exception{
        avroByteArrayFromRandomObject.setAvroSchemaPath("src/main/resources/avro/com/aksh/kafka/avro/fake/TradeData.avsc");
        avroByteArrayFromRandomObject.setFakeGeneratorJSScript("src/main/resources/generate-data.js");
        ByteBuffer bufffer=avroByteArrayFromRandomObject.generateData();


        Properties dataOut= avroSerDe.deserializeToProperties(new FileInputStream("src/main/resources/avro/com/aksh/kafka/avro/fake/TradeData.avsc"),bufffer);
        System.out.println("Object output properties:"+dataOut);


    }

    @Test
    public void deserialize() {
    }
}