package com.j2clark.jstream.kinesis.analytics;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.avro.Schema;
import org.apache.flink.api.common.typeinfo.Types;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.windowing.assigners.SlidingProcessingTimeWindows;
import org.apache.flink.streaming.api.windowing.time.Time;
import org.apache.flink.streaming.connectors.kinesis.FlinkKinesisConsumer;
import org.apache.flink.streaming.connectors.kinesis.FlinkKinesisProducer;
import org.apache.flink.streaming.connectors.kinesis.config.AWSConfigConstants;
import org.apache.flink.streaming.connectors.kinesis.config.ConsumerConfigConstants;
import org.apache.flink.api.common.serialization.SimpleStringSchema;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.net.URL;
import java.util.Properties;

public class JsonFlinkApp {

    private static final String region = "us-east-1";
    private static final String sourceStreamName = "stocktrade";
    private static final String sinkStreamName = "stockprice";

    private static FlinkKinesisConsumer<String> configureConsumer() {
        Properties consumerConfig = new Properties();
        consumerConfig.put(AWSConfigConstants.AWS_REGION, region);
        /*consumerConfig.put(AWSConfigConstants.AWS_ACCESS_KEY_ID, "aws_access_key_id");
        consumerConfig.put(AWSConfigConstants.AWS_SECRET_ACCESS_KEY, "aws_secret_access_key");*/
        consumerConfig.put(ConsumerConfigConstants.STREAM_INITIAL_POSITION, "LATEST");

        FlinkKinesisConsumer<String> consumer = new FlinkKinesisConsumer<>(
                sourceStreamName,
                new SimpleStringSchema(),
                consumerConfig
        );

        return consumer;
    }

    private static FlinkKinesisProducer<String> configureProducer() {
        Properties producerConfig = new Properties();
        producerConfig.put(AWSConfigConstants.AWS_REGION, region);
        /*producerConfig.put(AWSConfigConstants.AWS_ACCESS_KEY_ID, "aws_access_key_id");
        producerConfig.put(AWSConfigConstants.AWS_SECRET_ACCESS_KEY, "aws_secret_access_key");*/
        producerConfig.put("AggregationEnabled", "false");

        FlinkKinesisProducer<String> producer = new FlinkKinesisProducer<>(
                new SimpleStringSchema(),
                producerConfig
        );
        producer.setFailOnError(true);
        producer.setDefaultStream(sinkStreamName);
        producer.setDefaultPartition("0");
        return producer;
    }

    public static void main(String[] args) throws Exception {
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        env.enableCheckpointing(5000);
        //this is now the default env.setStreamTimeCharacteristic(TimeCharacteristic.EventTime);

        FlinkKinesisConsumer<String> consumer = configureConsumer();
        FlinkKinesisProducer<String> producer = configureProducer();
        DataStream<String> stream = env.addSource(consumer);
        stream.addSink(producer);

        ObjectMapper jsonParser = new ObjectMapper();

        // stream logic goes here
        stream.map(value -> {
                    JsonNode jsonNode = jsonParser.readValue(value, JsonNode.class);
                    String tickerSymbol = jsonNode.get("tickerSymbol").asText();
                    Double price = jsonNode.get("price").asDouble();
                    return new Tuple2<>(tickerSymbol, price);
                })
                .returns(Types.TUPLE(Types.STRING, Types.DOUBLE))
                .keyBy(0)
                .window(SlidingProcessingTimeWindows.of(Time.seconds(10), Time.seconds(5)))
                .min(1)
                .setParallelism(3)
                .map(value -> value.f0 + String.format(",%.2f", value.f1) + "\n")
                .addSink(producer);

        env.execute();
    }


}
