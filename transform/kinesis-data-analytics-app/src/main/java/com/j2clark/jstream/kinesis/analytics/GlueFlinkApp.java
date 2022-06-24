package com.j2clark.jstream.kinesis.analytics;

import com.amazonaws.services.schemaregistry.flink.avro.GlueSchemaRegistryAvroDeserializationSchema;
import com.amazonaws.services.schemaregistry.flink.avro.GlueSchemaRegistryAvroSerializationSchema;
import com.amazonaws.services.schemaregistry.utils.AWSSchemaRegistryConstants;
import com.amazonaws.services.schemaregistry.utils.AvroRecordType;
import org.apache.avro.Schema;
import org.apache.avro.generic.GenericRecord;
import org.apache.avro.generic.GenericRecordBuilder;
import org.apache.flink.api.common.typeinfo.Types;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.windowing.assigners.SlidingProcessingTimeWindows;
import org.apache.flink.streaming.api.windowing.time.Time;
import org.apache.flink.streaming.connectors.kinesis.FlinkKinesisConsumer;
import org.apache.flink.streaming.connectors.kinesis.config.AWSConfigConstants;
import org.apache.flink.streaming.connectors.kinesis.config.ConsumerConfigConstants;

import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.connectors.kinesis.FlinkKinesisProducer;


import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public class GlueFlinkApp {

    private static final String region = "us-east-1";
    private static final String sourceStreamName = "stocktrade";
    private static final String sinkStreamName = "stockprice";

    private static final String sourceSchemaName  = "source.avsc";
    private static final String sinkSchemaName  = "sink.avsc";

    private static FlinkKinesisConsumer<GenericRecord> configureConsumer(Schema schema/*String schemaDefinition*/) {
        Properties consumerConfig = new Properties();
        consumerConfig.put(AWSConfigConstants.AWS_REGION, region);
        /*consumerConfig.put(AWSConfigConstants.AWS_ACCESS_KEY_ID, "aws_access_key_id");
        consumerConfig.put(AWSConfigConstants.AWS_SECRET_ACCESS_KEY, "aws_secret_access_key");*/
        consumerConfig.put(ConsumerConfigConstants.STREAM_INITIAL_POSITION, "LATEST");

        Map<String, Object> schemaConfig = new HashMap<>();
        schemaConfig.put(AWSSchemaRegistryConstants.AWS_REGION, region);
        schemaConfig.put(AWSSchemaRegistryConstants.SCHEMA_AUTO_REGISTRATION_SETTING, true);
        schemaConfig.put(AWSSchemaRegistryConstants.AVRO_RECORD_TYPE, AvroRecordType.GENERIC_RECORD.getName());

        //Schema schema = new Schema.Parser().parse(schemaDefinition);

        FlinkKinesisConsumer<GenericRecord> consumer = new FlinkKinesisConsumer<>(
                sourceStreamName,
                // block 2
                GlueSchemaRegistryAvroDeserializationSchema.forGeneric(schema, schemaConfig),
                consumerConfig
        );

        /*JobManagerWatermarkTracker watermarkTracker = new JobManagerWatermarkTracker(sourceStreamName);
        consumer.setWatermarkTracker(watermarkTracker);*/

        return consumer;
    }

    private static FlinkKinesisProducer<GenericRecord> configureProducer(Schema schema/*String schemaDefinition*/) {
        Properties producerConfig = new Properties();
        producerConfig.put(AWSConfigConstants.AWS_REGION, region);
        /*producerConfig.put(AWSConfigConstants.AWS_ACCESS_KEY_ID, "aws_access_key_id");
        producerConfig.put(AWSConfigConstants.AWS_SECRET_ACCESS_KEY, "aws_secret_access_key");*/
        producerConfig.put("AggregationEnabled", "false");

        /*producerConfig.put("AggregationMaxCount", "4294967295");
        producerConfig.put("CollectionMaxCount", "1000");
        producerConfig.put("RecordTtl", "30000");
        producerConfig.put("RequestTimeout", "6000");
        producerConfig.put("ThreadPoolSize", "15");*/


        Map<String, Object> schemaConfig = new HashMap<>();
        schemaConfig.put(AWSSchemaRegistryConstants.AWS_REGION, region);
        schemaConfig.put(AWSSchemaRegistryConstants.SCHEMA_AUTO_REGISTRATION_SETTING, true);
        schemaConfig.put(AWSSchemaRegistryConstants.AVRO_RECORD_TYPE, AvroRecordType.GENERIC_RECORD.getName());

        //Schema schema = new Schema.Parser().parse(schemaDefinition);

        FlinkKinesisProducer<GenericRecord> producer = new FlinkKinesisProducer<>(
                // block 3
                GlueSchemaRegistryAvroSerializationSchema.forGeneric(schema, sinkStreamName, schemaConfig),
                producerConfig
        );
        producer.setFailOnError(true);
        producer.setDefaultStream(sinkStreamName);
        producer.setDefaultPartition("0");
        return producer;
    }

    public static Schema getSchema(String avscName) {
        ClassLoader classLoader = GlueFlinkApp.class.getClassLoader();
        URL resource = classLoader.getResource("source.avsc");
        if (resource != null) {
            try (InputStream is = resource.openStream()) {
                return new Schema.Parser().parse(is);
            } catch (IOException ioe) {
                throw new UncheckedIOException("Resource["+avscName+"] not found", ioe);
            }
        } else {
            throw new UncheckedIOException("Resource["+avscName+"] not found", new IOException());
        }
    }

    public static void main(String[] args) throws Exception {



        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        env.enableCheckpointing(5000);
        //this is now the default env.setStreamTimeCharacteristic(TimeCharacteristic.EventTime);

        Schema sourceSchema = getSchema(sourceSchemaName);
        FlinkKinesisConsumer<GenericRecord> consumer = configureConsumer(sourceSchema);

        Schema sinkSchema = getSchema(sinkSchemaName);
        FlinkKinesisProducer<GenericRecord> producer = configureProducer(sinkSchema);
        DataStream<GenericRecord> stream = env.addSource(consumer);
        stream.addSink(producer);

        //ObjectMapper jsonParser = new ObjectMapper();

        // stream logic goes here
        stream.map(genericRecord -> {
                    //JsonNode jsonNode = jsonParser.readValue(value, JsonNode.class);
                    String tickerSymbol = genericRecord.get("tickerSymbol").toString();
                    Double price = (Double) genericRecord.get("price");
                    return new Tuple2<>(tickerSymbol, price);
        })
                .returns(Types.TUPLE(Types.STRING, Types.DOUBLE))
                .keyBy(0)
                .window(SlidingProcessingTimeWindows.of(Time.seconds(10), Time.seconds(5)))
                .min(1)
                .setParallelism(3)
                .map(value -> {
                    //return value.f0 + String.format(",%.2f", value.f1) + "\n";
                    GenericRecordBuilder genericRecordBuilder = new GenericRecordBuilder(sinkSchema);
                    genericRecordBuilder.set("symbol", value.f0);
                    genericRecordBuilder.set("price", String.format(",%.2f", value.f1));
                    GenericRecord record = genericRecordBuilder.build();
                    return record;
                })
                .addSink(producer);

        env.execute();
    }

}
