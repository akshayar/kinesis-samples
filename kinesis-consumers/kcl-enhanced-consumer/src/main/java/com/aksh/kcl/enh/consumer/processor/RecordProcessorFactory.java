package com.aksh.kcl.enh.consumer.processor;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

import software.amazon.kinesis.processor.ShardRecordProcessor;
import software.amazon.kinesis.processor.ShardRecordProcessorFactory;

/**
 * Used to create new record processors.
 */
@Component
public class RecordProcessorFactory implements ShardRecordProcessorFactory,ApplicationContextAware {
    private ApplicationContext applicationContext;
	
    
    
    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
    	this.applicationContext=applicationContext;
    }


	@Override
	public ShardRecordProcessor shardRecordProcessor() {
		return applicationContext.getBean(SampleRecordProcessor.class);
	}
}