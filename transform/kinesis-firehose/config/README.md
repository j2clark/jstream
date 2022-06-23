# Sample Cloudformation Configurations

[Cloudformation: Kinesis Data Firehose and Lambda](https://thomasstep.com/blog/cloudformation-example-for-kinesis-data-firehose-and-lambda)


### [s3-destination.yml](s3-destination.yml)

s3-destination is a very simple sink to S3, formatting the prefix using the `timestamp` namespace.

```yaml
Type: AWS::KinesisFirehose::DeliveryStream
Properties:
  S3DestinationConfiguration:
    Prefix: data/date=!{timestamp:yyyy-MM-dd}/time=!{timestamp:HHmm}/
```

When evaluating timestamps, Kinesis Data Firehose uses the approximate arrival timestamp of the oldest record that's contained in the Amazon S3 object being written.

See [S3 Prefixes](https://docs.aws.amazon.com/firehose/latest/dev/s3-prefixes.html) for details

### [s3ext-destination.yml](s3ext-destination.yml)

s3ext-destination expands in s3-destination, and utilizes the data payload to define the s3 prefix

There are several significant differences from simple example, starting with configuration: instead of `S3DestinationConfiguration` we use `ExtendedS3DestinationConfiguration`

We have added a new path segment, `symbol=!{partitionKeyFromQuery:symbol}/` which is pulled from json payload

ProcessingConfiguration uses JQ to query the data (in this case just the payload) and extract data for use in prefix generation

See [S3 Prefixes](https://docs.aws.amazon.com/firehose/latest/dev/s3-prefixes.html) for details and examples

See [JQ v1.6](https://stedolan.github.io/jq/manual/v1.6/) for syntax 

### Example

Incoming record data: 
```json
{
  "TICKER_SYMBOL": "QXZ",
  "SECTOR": "HEALTHCARE",
  "CHANGE": -0.05,
  "PRICE": 84.51
 }
```

The following configuration extracts the value of `TICKER_SYMBOL` and assigns it to `symbol` 
```yaml
Type: MetadataExtraction
Parameters:
  - ParameterName: MetadataExtractionQuery
    ParameterValue: '{symbol: .TICKER_SYMBOL}'
  - ParameterName: JsonParsingEngine
    ParameterValue: JQ-1.6
```

```json
{
  "symbol": "QXZ"
}
```

We can now use the namespaced key `{partitionKeyFromQuery:symbol}` to build our prefix path:

```shell
data/date=!2022-06-15/time=0100/symbol=QXZ/
```

Relevant changes to config are as follows:

```yaml
Type: AWS::KinesisFirehose::DeliveryStream
Properties:
    ExtendedS3DestinationConfiguration:      
      Prefix: data/date=!{timestamp:yyyy-MM-dd}/time=!{timestamp:HHmm}/symbol=!{partitionKeyFromQuery:symbol}/
      DynamicPartitioningConfiguration:
        Enabled: true
      ProcessingConfiguration:
        Enabled: true
        Processors:
          - Type: MetadataExtraction
            Parameters:
              - ParameterName: MetadataExtractionQuery
                ParameterValue: '{symbol: .TICKER_SYMBOL}'
              - ParameterName: JsonParsingEngine
                ParameterValue: JQ-1.6
```





