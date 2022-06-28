# Kinesis Data Stream Analytics Application

TODO: 
1. Get Getting Started application working and tested (use [[cloudformation.yml](cf/application.yml) stack)
2. Adapt to write to Stream
3. Use GlueSchemaRepository for input/output



An application which reads and process 1 or more kinesis data streams.

This is a Flink Application

Support integration with Glue Schema Registry

Use Cases:

Transform data stream into 1 or more records, validated by schema

[Integrating with AWS Glue Schema Registry, Use Case: Amazon Kinesis Data Analytics for Apache Flink](https://docs.aws.amazon.com/glue/latest/dg/schema-registry-integrations.html#schema-registry-integrations-kds)

[Amazon AWS Kinesis Streams Connector](https://nightlies.apache.org/flink/flink-docs-release-1.11/dev/connectors/kinesis.html)

[aws-samples/amazon-kinesis-data-analytics-java-examples](https://github.com/aws-samples/amazon-kinesis-data-analytics-java-examples)

Amazon Kinesis Data Analytics Developer Guid Documentation [Step 3: Create and Run a Kinesis Data Analytics for Apache Flink Application](https://docs.aws.amazon.com/kinesisanalytics/latest/java/get-started-exercise.html) and github repo [aws-samples/amazon-kinesis-data-analytics-java-examples](https://github.com/aws-samples/amazon-kinesis-data-analytics-java-examples) do not line up. I start in [GettingStarted_1_11](https://github.com/aws-samples/amazon-kinesis-data-analytics-java-examples/tree/master/GettingStarted_1_11)


## Quick Start

A relatively quick-start application: [Amazon Kinesis Data Analytics Flink – Starter Kit](https://github.com/aws-samples/amazon-kinesis-data-analytics-flink-starter-kit)

A utility to create streaming data for quick-start app: [Amazon Kinesis Data Analytics Flink – Benchmarking Utility](https://github.com/aws-samples/amazon-kinesis-data-analytics-flink-benchmarking-utility)

```shell
aws s3 cp kinesis-data-analytics-app-1.0.jar s3://firehose-deliverystream-test/code/kinesis-data-analytics-app-1.0.jar

aws kinesis create-stream --stream-name kda_flink_starter_kit_kinesis_stream --shard-count 4
```

## DEV NOTES

I had issues creating analytics role AND application in same cloudformation template - access to the s3 code was not allowed

Separating role and application into separate configs seems to have solved this. I spent quite a bit of time trying to narrow down issue, but needed to move on.



