
[Build and run streaming applications with Apache Flink](https://aws.amazon.com/blogs/big-data/build-and-run-streaming-applications-with-apache-flink-and-amazon-kinesis-data-analytics-for-java-applications/)

The following [Flink ETL Example](https://aws.amazon.com/blogs/big-data/streaming-etl-with-apache-flink-and-amazon-kinesis-data-analytics/) seems to be an up-to-date example of flink application



Flink Reference in AWS docs is out of date and borderline incomprehensible

```
It is only relevant to know that you can create a Kinesis Data Analytics application by uploading the compiled Flink application jar file to Amazon S3 and specifying some additional configuration options with the service. 
You can then execute the Kinesis Data Analytics application in a fully managed environment. 
For more information, see Build and run streaming applications with Apache Flink and Amazon Kinesis Data Analytics for Java Applications and the Amazon Kinesis Data Analytics developer guide.
```

```
To connect to a Kinesis data stream, first configure the Region and a credentials provider. 
As a general best practice, choose AUTO as the credentials provider. 
The application will then use temporary credentials from the role of the respective Kinesis Data Analytics application to read events from the specified data stream. 
This avoids baking static credentials into the application.
```


```shell
aws kinesis create-stream  --stream-name ExampleInputStream  --shard-count 1  --region us-west-1 --profile adminuser

aws kinesis create-stream --stream-name ExampleOutputStream --shard-count 1 --region us-west-1  --profile adminuser
```

