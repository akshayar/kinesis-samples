package com.aksh.kinesis.producer.publisher;

import com.aksh.kinesis.producer.faker.JSRandomDataGenerator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.nio.ByteBuffer;
import java.util.Properties;
@Component
public class JsonDataFromRandomObjectGenStrategy implements DataGenerationStrategy {
    @Autowired
    JSRandomDataGenerator jsRandomDataGenerator;
    @Value("${faker.fakeGeneratorJSScript:src/main/resources/generate-data.js}")
    private String fakeGeneratorJSScript;
    @Override
    public ByteBuffer generateData() throws Exception {
        return getGenerateDataAsByteBuffer();
    }

    private ByteBuffer getGenerateDataAsByteBuffer() throws Exception {
        String payload = jsRandomDataGenerator.createPayload(null,fakeGeneratorJSScript);
        System.out.println("Data Push: " + payload);
        ByteBuffer data = ByteBuffer.wrap(payload.getBytes("UTF-8"));
        return data;
    }
    public ByteBuffer generateData(Properties properties) throws Exception{
        return getGenerateDataAsByteBuffer();
    }
}
