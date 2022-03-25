package com.aksh.kinesislambda;


import com.aksh.kinesislambda.handler.Handler;
import com.amazonaws.kinesis.deagg.RecordDeaggregator;
import com.amazonaws.services.kinesis.clientlibrary.types.UserRecord;
import com.amazonaws.services.kinesisanalytics.model.KinesisFirehoseInputUpdate;
import com.amazonaws.services.kinesisanalytics.model.KinesisFirehoseOutput;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.KinesisAnalyticsInputPreprocessingResponse;
import com.amazonaws.services.lambda.runtime.events.KinesisEvent;
import com.amazonaws.services.lambda.runtime.events.KinesisFirehoseEvent;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import java.nio.ByteBuffer;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class FirehoseLambdaFunctionHandler implements RequestHandler<KinesisFirehoseEvent, KinesisAnalyticsInputPreprocessingResponse> {
    Handler handler;
    ApplicationContext applicationContext;


    public FirehoseLambdaFunctionHandler() {
        this(new AnnotationConfigApplicationContext(BeanConfig.class));
    }

    public FirehoseLambdaFunctionHandler(ApplicationContext apContext) {
        this.applicationContext = apContext;
        handler = applicationContext.getBean(Handler.class);
    }

    @Override
    public KinesisAnalyticsInputPreprocessingResponse handleRequest(KinesisFirehoseEvent event, Context context) {
        context.getLogger().log("Input: " + event);
        List<KinesisAnalyticsInputPreprocessingResponse.Record> records = event.getRecords().stream().map(firehoseRecord -> {
            if (handler.validateFirehoseRecord(firehoseRecord)) {
                return new KinesisAnalyticsInputPreprocessingResponse.Record(firehoseRecord.getRecordId(), KinesisAnalyticsInputPreprocessingResponse.Result.Ok, firehoseRecord.getData());
            } else {
                return new KinesisAnalyticsInputPreprocessingResponse.Record(firehoseRecord.getRecordId(), KinesisAnalyticsInputPreprocessingResponse.Result.Ok, firehoseRecord.getData());

            }
        }).collect(Collectors.toList());

        return new KinesisAnalyticsInputPreprocessingResponse(records);
    }

    private void processErrorRecords(List<UserRecord> errorRecords, Context context) {
        context.getLogger().log("Error Records: " + errorRecords);
    }

    public ApplicationContext getApplicationContext() {
        return applicationContext;
    }
}
