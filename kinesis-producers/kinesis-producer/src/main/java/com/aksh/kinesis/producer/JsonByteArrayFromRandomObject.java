package com.aksh.kinesis.producer;

import com.aksh.kinesis.producer.fake.JSRandomDataGenerator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.nio.ByteBuffer;
import java.util.Properties;
@Component
public class JsonByteArrayFromRandomObject implements ByteArrayGenerationStrategy{
    @Autowired
    JSRandomDataGenerator jsRandomDataGenerator;
    @Value("${fakeGeneratorJSScript:src/main/resources/generate-data.js}")
    private String fakeGeneratorJSScript;
    @Override
    public ByteBuffer generateData() throws Exception {
        return getGenerateDataAsByteBuffer();
    }

    private ByteBuffer getGenerateDataAsByteBuffer() throws Exception {
        String payload = jsRandomDataGenerator.createPayload();
        System.out.println("Data Push: " + payload);
        ByteBuffer data = ByteBuffer.wrap(payload.getBytes("UTF-8"));
        return data;
    }
    public ByteBuffer generateData(Properties properties) throws Exception{
        return getGenerateDataAsByteBuffer();
    }
}
