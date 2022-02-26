package com.aksh.kinesislambda;



import com.amazonaws.kinesis.deagg.RecordDeaggregator;
import com.amazonaws.services.kinesis.clientlibrary.types.UserRecord;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import com.aksh.kinesislambda.handler.Handler;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.KinesisEvent;
import com.amazonaws.services.lambda.runtime.events.KinesisEvent.KinesisEventRecord;

import java.util.List;

public class LambdaFunctionHandler implements RequestHandler<KinesisEvent, Integer> {
	Handler handler;
	ApplicationContext applicationContext;


	public LambdaFunctionHandler() {
		this(new AnnotationConfigApplicationContext(BeanConfig.class));
	}
	
	public LambdaFunctionHandler(ApplicationContext apContext) {
		this.applicationContext=apContext;
		handler=applicationContext.getBean(Handler.class);
	}

    @Override
    public Integer handleRequest(KinesisEvent event, Context context) {
        context.getLogger().log("Input: " + event);
		new RecordDeaggregator().deaggregate(event.getRecords()).stream().forEach(handler::handle);
        return event.getRecords().size();
    }
    
    public ApplicationContext getApplicationContext() {
		return applicationContext;
	}
}
