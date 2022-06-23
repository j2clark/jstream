package com.j2clark.jstream.kinesis.function;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.KinesisAnalyticsInputPreprocessingResponse;
import com.amazonaws.services.lambda.runtime.events.KinesisFirehoseEvent;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

public class LambdaKinesisTransform implements RequestHandler<KinesisFirehoseEvent, KinesisAnalyticsInputPreprocessingResponse> {

    private final static ObjectMapper JSON = new ObjectMapper();
    static {
        JSON.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    @Override
    public KinesisAnalyticsInputPreprocessingResponse handleRequest(KinesisFirehoseEvent kinesisFirehoseEvent, Context context) {

        LambdaLogger logger = context.getLogger();

        try {
            logger.log("Kinesis FirehoseEvent: " + JSON.writeValueAsString(kinesisFirehoseEvent) + "\n");
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }

        List<KinesisAnalyticsInputPreprocessingResponse.Record> transformedRecords = new ArrayList<>();

        kinesisFirehoseEvent.getRecords().forEach(r -> {
            ByteBuffer byteBuffer = r.getData();
            while(byteBuffer.hasRemaining()) {
                byte[] arr = new byte[byteBuffer.remaining()];
                byteBuffer.get(arr);
                String jsonString = new String(arr);

                logger.log("Noop Transform on record: " + jsonString + "\n");

                byte[] transformedBytes = jsonString.getBytes();

                KinesisAnalyticsInputPreprocessingResponse.Record transformedRecord = new KinesisAnalyticsInputPreprocessingResponse.Record();
                transformedRecord.setData(ByteBuffer.wrap(transformedBytes));
                transformedRecord.setRecordId(r.getRecordId());
                transformedRecord.setResult(KinesisAnalyticsInputPreprocessingResponse.Result.Ok);

                transformedRecords.add(transformedRecord);
            }
        });

        logger.log("Returning "+transformedRecords.size()+" transformed records from "+kinesisFirehoseEvent.getRecords().size()+" source records\n");
        KinesisAnalyticsInputPreprocessingResponse response = new KinesisAnalyticsInputPreprocessingResponse();
        response.setRecords(transformedRecords);
        return response;
    }
}
