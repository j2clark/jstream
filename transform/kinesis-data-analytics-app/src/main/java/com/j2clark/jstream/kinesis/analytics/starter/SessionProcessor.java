package com.j2clark.jstream.kinesis.analytics.starter;

import java.util.Arrays;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

import org.apache.flink.api.common.serialization.SimpleStringEncoder;
import org.apache.flink.api.common.serialization.SimpleStringSchema;
import org.apache.flink.api.java.utils.ParameterTool;
import org.apache.flink.core.fs.Path;
import org.apache.flink.shaded.jackson2.com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.flink.streaming.api.TimeCharacteristic;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.datastream.KeyedStream;
import org.apache.flink.streaming.api.environment.LocalStreamEnvironment;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.sink.filesystem.StreamingFileSink;
import org.apache.flink.streaming.api.functions.sink.filesystem.rollingpolicies.DefaultRollingPolicy;
import org.apache.flink.streaming.api.windowing.assigners.ProcessingTimeSessionWindows;
import org.apache.flink.streaming.api.windowing.time.Time;
import org.apache.flink.streaming.connectors.kinesis.FlinkKinesisConsumer;
import org.apache.flink.streaming.connectors.kinesis.config.ConsumerConfigConstants;

import com.amazonaws.services.kinesisanalytics.runtime.KinesisAnalyticsRuntime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class SessionProcessor {

    public static final String S3_OUTPUT_PATH = "s3_output_path";

    public static final String BUCKET_CHECK_INTERVAL = "bucket_check_interval_in_seconds";
    public static final String ROLLING_INTERVAL = "rolling_interval_in_seconds";
    public static final String INACTIVITY_INTERVAL = "inactivity_interval_in_seconds";

    public static final String SESSION_TIMEOUT = "session_time_out_in_minutes";

    private static final Logger log = LoggerFactory.getLogger(SessionProcessor.class);

    /**
     * Main method and the entry point for Kinesis Data Analytics Flink Application.
     */
    public static void main(String[] args) throws Exception {
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        ParameterTool parameter;
        if (env instanceof LocalStreamEnvironment) {
            parameter = ParameterTool.fromArgs(args);
        } else {
            // read properties from Kinesis Data Analytics environment
            Map<String, Properties> applicationProperties = KinesisAnalyticsRuntime.getApplicationProperties();
            Properties flinkProperties = applicationProperties.get("FlinkAppProperties");
            if (flinkProperties == null) {
                throw new RuntimeException("Unable to load properties from Group ID FlinkAppProperties.");
            }
            parameter = ParameterToolUtils.fromApplicationProperties(flinkProperties);
        }

        if (!validateRuntimeProperties(parameter))
            throw new RuntimeException(
                    "Runtime properties are invalid. Will not proceed to start Kinesis Analytics Application");
        env.setStreamTimeCharacteristic(TimeCharacteristic.IngestionTime);
        env.registerType(StockTrade.class);
        DataStream<String> stream = createKinesisSource(env, parameter);
        log.info("Kinesis stream created.");

        ObjectMapper objectMapper = new ObjectMapper();
        KeyedStream<StockTrade, String> keyedStream = stream.map(record -> {
            try {
                return objectMapper.readValue(record, StockTrade.class);
            } catch (Exception e) {
                log.error("Exception in parsing the input records to Event POJO. "
                        + "Please make sure the input record structure is compatible with the POJO. Input record: "
                        + record);
                return null;
            }
        }).filter(Objects::nonNull).keyBy(StockTrade::getTickerSymbol);

        /**
         * EventTimeSessionWindows - The timestamp when the event occurred. This is also
         * sometimes called the client-side time.
         *
         * Ingest time – The timestamp of when record was added to the streaming source.
         * Amazon Kinesis Data Streams includes a field called APPROXIMATE_ARRIVAL_TIME
         * in every record that provides this timestamp. This is also sometimes referred
         * to as the server-side time.
         *
         * Source:
         * https://docs.aws.amazon.com/kinesisanalytics/latest/dev/timestamps-rowtime-concepts.html
         */
        long timeout = Long.parseLong(parameter.get("session_time_out_in_minutes"));
        DataStream<String> sessionStream = keyedStream
                .window(ProcessingTimeSessionWindows.withGap(Time.minutes(timeout))).aggregate(new Aggregator())
                .name("session_stream");
        sessionStream.addSink(createS3Sink(parameter)).name("session_processor_sink");
        log.info("S3 Sink added.");
        env.execute("Kinesis Data Analytics Flink Application with  Session Window and Aggregate Function");
    }

    /**
     * Method creates Kinesis source based on Application properties
     */
    private static DataStream<String> createKinesisSource(StreamExecutionEnvironment env, ParameterTool paramTool) {
        log.info("Creating Kinesis source from Application Properties");
        Properties inputProperties = new Properties();
        inputProperties.setProperty(ConsumerConfigConstants.AWS_REGION, paramTool.get("region"));
        inputProperties.setProperty(ConsumerConfigConstants.STREAM_INITIAL_POSITION,
                paramTool.get("stream_init_position"));
        if (paramTool.get("stream_init_position").equalsIgnoreCase(StreamPosition.AT_TIMESTAMP.name())) {
            inputProperties.setProperty(ConsumerConfigConstants.STREAM_INITIAL_TIMESTAMP,
                    paramTool.get("stream_initial_timestamp"));
        }

        return env.addSource(new FlinkKinesisConsumer<>(paramTool.get("input_stream_name"), new SimpleStringSchema(),
                inputProperties));
    }

    /**
     * Method creates S3 sink based on application properties
     */
    private static StreamingFileSink<String> createS3Sink(ParameterTool parameter) {
        log.info("Creating S3 sink from Application Properties");
        final StreamingFileSink<String> sink = StreamingFileSink
                .forRowFormat(new Path(parameter.get(S3_OUTPUT_PATH)), new SimpleStringEncoder<String>("UTF-8"))
                .withBucketCheckInterval(
                        TimeUnit.SECONDS.toMillis(Long.parseLong(parameter.get("bucket_check_interval_in_seconds"))))
                .withRollingPolicy(DefaultRollingPolicy.create()
                        .withRolloverInterval(
                                TimeUnit.SECONDS.toMillis(Long.parseLong(parameter.get("rolling_interval_in_seconds"))))
                        .withInactivityInterval(TimeUnit.SECONDS
                                .toMillis(Long.parseLong(parameter.get("inactivity_interval_in_seconds"))))
                        .build())
                .build();
        return sink;
    }

    /**
     * Method validates runtime properties
     */
    private static boolean validateRuntimeProperties(ParameterTool paramTool) {

        boolean bucketExist = false;
        boolean propertiesValid = false;
        boolean initialTimestampNAOrValidIfPresent = false;

        try {
            log.info("Printing runtime Properties to CloudWatch");
            paramTool.toMap().forEach((key, value) -> log.info("parameter: " + key + ", value: " + value));
            bucketExist = SessionUtil.checkIfBucketExist(paramTool.get("region"), paramTool.get("s3_output_path"));
            long sessionTimeout = Long.parseLong(paramTool.get("session_time_out_in_minutes"));
            boolean streamExist = SessionUtil.checkIfStreamExist(paramTool.get("region"),
                    paramTool.get("input_stream_name"));

            // Check if stream_init_position is valid
            boolean streamInitPositionValid = Arrays.stream(StreamPosition.values())
                    .anyMatch((t) -> t.name().equals(paramTool.get("stream_init_position")));

            if (streamInitPositionValid) {
                if (paramTool.get("stream_init_position").equalsIgnoreCase(StreamPosition.AT_TIMESTAMP.name())) {
                    if (Optional.ofNullable(paramTool.get("stream_initial_timestamp")).isPresent()) {
                        if (SessionUtil.validateDate(paramTool.get("stream_initial_timestamp")))
                            initialTimestampNAOrValidIfPresent = true;
                    } else
                        log.error(
                                "stream_init_position is set to 'AT_TIMESTAMP' but 'stream_initial_timestamp' is not provided");
                } else
                    initialTimestampNAOrValidIfPresent = true;
            }
            // Check if all conditions are met
            if (sessionTimeout != 0L && streamExist && bucketExist && streamInitPositionValid
                    && initialTimestampNAOrValidIfPresent) {
                propertiesValid = true;
                log.info("Runtime properties are valid.");
            } else {
                log.error("Runtime properties are not valid.");
                if (!streamExist)
                    log.error(
                            "The specified Kinesis stream: " + paramTool.get("input_stream_name") + "does not exist.");
                if (!bucketExist)
                    log.error("The specified s3 bucket: " + paramTool.get("s3_output_path") + "does not exist.");
            }
        } catch (NumberFormatException e) {
            log.error("Value for property 'session_time_out_in_minutes' is invalid");
            e.printStackTrace();
        }
        if (propertiesValid) {
            log.info("KDA Flink Application will consume data from: " + paramTool.get("stream_init_position"));
            if (paramTool.get("stream_init_position").equalsIgnoreCase(StreamPosition.AT_TIMESTAMP.name())) {
                log.info("The 'STREAM_INITIAL_TIMESTAMP' is set to: " + paramTool.get("stream_initial_timestamp"));
            }

        }
        return propertiesValid;
    }

}
