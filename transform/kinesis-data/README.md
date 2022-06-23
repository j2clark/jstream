# kinesis-data

kinesis data is used to execute data transformations which are published to a new data stream

[AWS Developer Guide](https://docs.aws.amazon.com/streams/latest/dev/introduction.html)

[API Reference](https://docs.aws.amazon.com/kinesis/latest/APIReference/Welcome.html)

### Similar yet different to Kafka

Partition Keys behave similar to kafka, and yet different enough to have to be careful

From the [docs](https://docs.aws.amazon.com/streams/latest/dev/key-concepts.html):
```
Partition Key

A partition key is used to group data by shard within a stream. 
Kinesis Data Streams segregates the data records belonging to a stream into multiple shards. 
It uses the partition key that is associated with each data record to determine which shard a given data record belongs to. 
Partition keys are Unicode strings, with a maximum length limit of 256 characters for each key. 
An MD5 hash function is used to map partition keys to 128-bit integer values and to map associated data records to shards using the hash key ranges of the shards. 
When an application puts data into a stream, it must specify a partition key.
```

### Important limits:
```
A single shard can ingest up to 1 MB of data per second (including partition keys) or 1,000 records per second for writes. 
Similarly, if you scale your stream to 5,000 shards, the stream can ingest up to 5 GB per second or 5 million records per second.
```

```The maximum size of the data payload of a record before base64-encoding is up to 1 MB.```

```
Each shard can support up to a maximum total data read rate of 2 MB per second via GetRecords. 
If a call to GetRecords returns 10 MB, subsequent calls made within the next 5 seconds throw an exception.
```

## Development

[How to: Java Producer](https://docs.aws.amazon.com/streams/latest/dev/developing-producers-with-sdk.html)

[How to: Java Consumer](https://docs.aws.amazon.com/streams/latest/dev/developing-consumers-with-sdk.html)

[Glue interaction](https://docs.aws.amazon.com/glue/latest/dg/schema-registry-integrations.html#schema-registry-integrations-kds)

[On-Premise Writes](https://medium.com/globant/ingesting-data-in-kinesis-data-streams-using-kpl-for-on-premise-application-e1957a3bfe59)


## AWS CLI

```shell
aws kinesis help
```

```shell
aws kinesis create-stream --stream-name Foo

aws kinesis describe-stream-summary --stream-name Foo

aws kinesis list-streams

aws kinesis put-record --stream-name Foo --partition-key 123 --data testdata

aws kinesis get-shard-iterator --shard-id shardId-000000000000 --shard-iterator-type TRIM_HORIZON --stream-name Foo
# Example Response:
#{
#    "ShardIterator": "AAAAAAAAAAHXJ2mgKKSmqwkvD8WBvW/V86jIpWcmeNzp2SEB/hg9RckjpVTBR1x+NfSCTbs3hnkNwC1bFumkLX8Qf0w5mGTRA46+Tl9b82KuWqKfG3Gcz83YV9nQWTMPEDea8fz+a88cW9xcj/Q9IMcjKMpBsy5gQmNvFeUfiKLszmCmqbQ5K5VRGiiqH/144YKsX4XoO5rx7+W49LLzlExPpaUpx2LyABqZfugyP0daBPs8qjH2Kg=="
#}
aws kinesis get-records --shard-iterator AAAAAAAAAAHXJ2mgKKSmqwkvD8WBvW/V86jIpWcmeNzp2SEB/hg9RckjpVTBR1x+NfSCTbs3hnkNwC1bFumkLX8Qf0w5mGTRA46+Tl9b82KuWqKfG3Gcz83YV9nQWTMPEDea8fz+a88cW9xcj/Q9IMcjKMpBsy5gQmNvFeUfiKLszmCmqbQ5K5VRGiiqH/144YKsX4XoO5rx7+W49LLzlExPpaUpx2LyABqZfugyP0daBPs8qjH2Kg==
#{
#    "Records": [
#        {
#            "SequenceNumber": "49630588007894325198556262772722560835479629979451392002",
#            "ApproximateArrivalTimestamp": "2022-06-18T12:22:08.120000-07:00",
#            "Data": "testdata",
#            "PartitionKey": "123"
#        }
#    ],
#    "NextShardIterator": "AAAAAAAAAAFZYrJV0i7stUTJkrQRErHxng43q0onMmcM4EStIGsqiDyf9TAPjnc+t3NEIH3XO+dpvhpRSzPmUifRAPdEzLvNDdI41lB6PiiRUIKgcumHatO+YhKCuOmXe7INTHrG9hD7PKfYQo5tRuRWJ4JVTbH2WkGei8dGgru8h8uo1MogarKQd9lpnHl6Fl/eLwTaJTSltcHYp4yynGyK/aPH8SfFJIpYXRIjuociYpi9nNngJA==",
#    "MillisBehindLatest": 0
#}

aws kinesis delete-stream --stream-name Foo


```

A note about NextShardIterator:
```
If you do not call get-records using the next shard iterator within the 300 second shard iterator lifetime, you will get an error message, and you will need to use the get-shard-iterator command to get a fresh shard iterator.
```

## Example Code
[Kinesis Data Analytics Java Examples](https://github.com/aws-samples/amazon-kinesis-data-analytics-java-examples)

[Streaming ETL with flink and kinesis data analytics](https://aws.amazon.com/blogs/big-data/streaming-etl-with-apache-flink-and-amazon-kinesis-data-analytics/)

## Connectors

[Kinesis Connectors](https://github.com/amazon-archives/amazon-kinesis-connectors)