package com.aksh.kinesis.producer;

import com.aksh.kafka.avro.fake.TradeData;
import com.aksh.kinesis.producer.avro.AvroSerDe;
import com.aksh.kinesis.producer.faker.FakeRandomGenerator;
import com.aksh.kinesis.producer.faker.JSRandomDataGenerator;
import org.junit.jupiter.api.Test;

import java.io.FileInputStream;
import java.nio.ByteBuffer;
import java.util.Properties;

public class GenericAvroSerDeTest {
    AvroSerDe avroSerDe =new AvroSerDe();

    @Test
    public void serialize() throws  Exception{
        JSRandomDataGenerator randomDataGenerator=new JSRandomDataGenerator();
        Properties data = randomDataGenerator.createPayloadObject(null,null);
        System.out.println("Object input properties:"+data);

        ByteBuffer bufffer= avroSerDe.serializeFromProperties(new FileInputStream("src/main/resources/avro/com/aksh/kafka/avro/fake/TradeData.avsc"),data);

        Properties dataOut= avroSerDe.deserializeToProperties(new FileInputStream("src/main/resources/avro/com/aksh/kafka/avro/fake/TradeData.avsc"),bufffer);
        System.out.println("Object output properties:"+dataOut);

        FakeRandomGenerator fakeRandomGenerator=new FakeRandomGenerator(randomDataGenerator);

        TradeData tradeData=(TradeData) fakeRandomGenerator.createPayloadObject(TradeData.class,null);
        System.out.println("Object input trade data:"+data);
        bufffer= avroSerDe.serialize(new FileInputStream("src/main/resources/avro/com/aksh/kafka/avro/fake/TradeData.avsc"),tradeData);
        avroSerDe.deserializeSpecific(new FileInputStream("src/main/resources/avro/com/aksh/kafka/avro/fake/TradeData.avsc"),bufffer);
        System.out.println("Object output trade data:"+dataOut);

    }

    @Test
    public void deserialize() {
    }
}