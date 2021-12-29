package com.aksh.kinesis.producer;

import com.aksh.kinesis.producer.faker.JSRandomDataGenerator;
import org.junit.jupiter.api.Test;

import java.util.Properties;

public class JSRandomDataGeneratorTest {
    JSRandomDataGenerator jsRandomDataGenerator =new JSRandomDataGenerator();

    @Test
    public void createPayloadObject() throws  Exception{
      Properties properties= jsRandomDataGenerator.createPayloadObject(null,"src/main/resources/generate-data-sensor.js");
      System.out.println(properties);
    }

}