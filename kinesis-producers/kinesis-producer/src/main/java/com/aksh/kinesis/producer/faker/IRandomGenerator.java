package com.aksh.kinesis.producer.faker;

public interface IRandomGenerator<T> {
    public String createPayload(Class<T> type,String templateFile) throws Exception;
    public  T createPayloadObject(Class<T> type,String templateFile) throws Exception;
    public String createPayload() throws Exception;
}
