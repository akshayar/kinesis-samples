package com.aksh.kinesis.producer.publisher;

import java.nio.ByteBuffer;
import java.util.Properties;

public interface ByteArrayGenerationStrategy {
    ByteBuffer generateData() throws Exception;
    ByteBuffer generateData(Properties properties) throws Exception;

}
