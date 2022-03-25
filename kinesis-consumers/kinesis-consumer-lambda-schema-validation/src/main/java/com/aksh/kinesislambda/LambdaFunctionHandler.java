package com.aksh.kinesislambda;


import com.aksh.kinesislambda.handler.Handler;
import com.amazonaws.kinesis.deagg.RecordDeaggregator;
import com.amazonaws.services.kinesis.clientlibrary.types.UserRecord;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.KinesisEvent;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import java.util.List;
import java.util.stream.Collectors;

public class LambdaFunctionHandler implements RequestHandler<KinesisEvent, Integer> {
    Handler handler;
    ApplicationContext applicationContext;


    public LambdaFunctionHandler() {
        this(new AnnotationConfigApplicationContext(BeanConfig.class));
    }

    public LambdaFunctionHandler(ApplicationContext apContext) {
        this.applicationContext = apContext;
        handler = applicationContext.getBean(Handler.class);
    }

    @Override
    public Integer handleRequest(KinesisEvent event, Context context) {
        context.getLogger().log("Input: " + event);
        List<UserRecord> errorRecords = new RecordDeaggregator().deaggregate(event.getRecords())
                .stream().filter(r->{
                    return ! handler.validateKinesisUserRecord(r);
                })
                .collect(Collectors.toList());
        processErrorRecords(errorRecords, context);
        return event.getRecords().size() - errorRecords.size();
    }

    private void processErrorRecords(List<UserRecord> errorRecords, Context context) {
        context.getLogger().log("Error Records: " + errorRecords);
    }

    public ApplicationContext getApplicationContext() {
        return applicationContext;
    }
}
