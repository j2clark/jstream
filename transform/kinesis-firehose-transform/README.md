# Firehose Transforming Delivery Stream

I keep going to this guide [cloudformation-example-for-kinesis-data-firehose-and-lambda](https://thomasstep.com/blog/cloudformation-example-for-kinesis-data-firehose-and-lambda) for help around roles. There is a link to cloudformation template I found helpful

[AWS: Building Lambda Functions with Java](https://docs.aws.amazon.com/lambda/latest/dg/lambda-java.html)

Helpful answer on what Event(s) to use for lambda transformations: [StackOverflow: Lambda Kinesis Events](https://stackoverflow.com/questions/51710546/kinesis-firehose-data-transformation-using-java)

## Example CloudFormation Solutions

### IAM

Example Stack for creating required IAM Roles and Policies

[iam.yml](cf/iam.yml)

### S3 Destination

Example Stack for creating an S3 Delivery Stream, with Transformation using Lambda Function

[s3-deliverystream.yml](cf/s3-deliverystream.yml)

### Complete Application Solution

Example all inclusive stack for creating IAM Roles/Policies, Firehose Delivery Streams and destinations  

[application-solution.yml](cf/application-solution.yml)

## Test Resources

### Lambda

Example Test Payload(s)

[kinesis-event](lambda/kinesis-event.json)

