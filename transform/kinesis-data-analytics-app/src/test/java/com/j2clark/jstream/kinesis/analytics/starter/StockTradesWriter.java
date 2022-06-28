package com.j2clark.jstream.kinesis.analytics.starter;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.amazonaws.services.kinesis.AmazonKinesis;
import com.amazonaws.services.kinesis.AmazonKinesisClientBuilder;
import com.amazonaws.services.kinesis.model.DescribeStreamResult;
import com.amazonaws.services.kinesis.model.PutRecordsRequest;
import com.amazonaws.services.kinesis.model.PutRecordsRequestEntry;
import com.amazonaws.services.kinesis.model.PutRecordsResult;

public class StockTradesWriter {

    public static void main(String[] args) throws Exception {
        String region = "us-east-1";
        String streamName = "ktest-analyticsstarterkit-sourcestream";
        String profile = "profile jamie-dev";

        AmazonKinesisClientBuilder clientBuilder = AmazonKinesisClientBuilder.standard();

        ProfileCredentialsProvider credentialsProvider = new ProfileCredentialsProvider(profile);
        clientBuilder.setCredentials(credentialsProvider);
        clientBuilder.setRegion(region);

        AmazonKinesis kinesisClient = clientBuilder.build();

        validateStream(kinesisClient, streamName);


        StockTradeGenerator stockTradeGenerator = new StockTradeGenerator();
        while(true) {
            PutRecordsRequest putRecordsRequest = new PutRecordsRequest();
            putRecordsRequest.setStreamName(streamName);
            List<PutRecordsRequestEntry> putRecordsRequestEntryList = new ArrayList<>();
            for (int i = 0; i < 100; i++) {
                PutRecordsRequestEntry putRecordsRequestEntry = new PutRecordsRequestEntry();
                StockTrade stockTrade = stockTradeGenerator.getRandomTrade();
                putRecordsRequestEntry.setData(ByteBuffer.wrap(stockTrade.toJsonAsBytes()));
                putRecordsRequestEntry.setPartitionKey(stockTrade.getTickerSymbol());
                //putRecordsRequestEntry.setData(ByteBuffer.wrap(String.valueOf(i).getBytes()));
                //putRecordsRequestEntry.setPartitionKey(String.format("partitionKey-%d", i));
                putRecordsRequestEntryList.add(putRecordsRequestEntry);
            }

            putRecordsRequest.setRecords(putRecordsRequestEntryList);
            PutRecordsResult putRecordsResult = kinesisClient.putRecords(putRecordsRequest);
            System.out.println("Put Result" + putRecordsResult);

            Thread.sleep(1000);
        }
    }

    public static void validateStream(AmazonKinesis kinesisClient, String streamName) {

        DescribeStreamResult response = kinesisClient.describeStream(streamName);
        System.out.println(response);

    }

}