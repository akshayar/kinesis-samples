package com.aksh.kcl.consumer.processor;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

import com.amazonaws.services.kinesis.clientlibrary.interfaces.IRecordProcessor;
import com.amazonaws.services.kinesis.clientlibrary.interfaces.IRecordProcessorFactory;

/**
 * Used to create new record processors.
 */
@Component
public class RecordProcessorFactory implements IRecordProcessorFactory,ApplicationContextAware {
    private ApplicationContext applicationContext;
	/**
     * {@inheritDoc}
     */
    public IRecordProcessor createProcessor() {
        return applicationContext.getBean(SampleRecordProcessor.class);
    }
    
    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
    	this.applicationContext=applicationContext;
    }
}