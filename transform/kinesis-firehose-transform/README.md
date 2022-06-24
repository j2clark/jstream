# Firehose Transforming Delivery Stream

I keep going to this guide [cloudformation-example-for-kinesis-data-firehose-and-lambda](https://thomasstep.com/blog/cloudformation-example-for-kinesis-data-firehose-and-lambda) for help around roles. There is a link to cloudformation template I found helpful

Helpful answer on what Event(s) to use for lambda transformations: [StackOverflow: Lambda Kinesis Events](https://stackoverflow.com/questions/51710546/kinesis-firehose-data-transformation-using-java)

[AWS: Building Lambda Functions with Java](https://docs.aws.amazon.com/lambda/latest/dg/lambda-java.html)

[AWS: Kinesis Firehose Developer Guide](https://docs.aws.amazon.com/firehose/latest/dev/what-is-this-service.html)

## Example CloudFormation Solutions

Helpful Cloudformation Resources:

[intrinsic functions](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/intrinsic-function-reference.html) (!GetAtt, !Join, !Sub, etc.)

[pseudo parameters](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/pseudo-parameter-reference.html) (AWS::AccountId, AWS::Region, etc.)

## Helpful Kinesis Links

[AWS::KinesisFirehose::DeliveryStream](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/aws-resource-kinesisfirehose-deliverystream.html) Describes where data is to be landed

Granting application access to resource
[Controlling Access with Amazon Kinesis Data Firehose](https://docs.aws.amazon.com/firehose/latest/dev/controlling-access.html)

### IAM

Example Stack for creating required IAM Roles and Policies

[iam.yml](cf/iam.yml)

### S3 Destination

Example Stack for creating an S3 Delivery Stream, with Transformation using Lambda Function

[s3-deliverystream.yml](cf/s3-deliverystream.yml)

### TODO: Glue Table Destination

Example Stack for creating a Glue Delivery Stream, with Transformation using Lambda Function

[AWS Glue: Developer Guide](https://docs.aws.amazon.com/glue/latest/dg/components-overview.html#data-catalog-intro)

[Creating AWS Glue resources using AWS CloudFormation templates](https://github.com/awsdocs/aws-glue-developer-guide/blob/master/doc_source/populate-with-cloudformation-templates.md#:~:text=Creating%20AWS%20Glue%20resources%20using%20AWS%20CloudFormation%20templates,Database%20with%20tables%20%205%20more%20rows%20)

[glue-deliverystream.yml](cf/glue-deliverystream.yml)

### Complete Application Solution

Example all-inclusive stack for creating IAM Roles/Policies, Firehose Delivery Streams and destinations  

[application-solution.yml](cf/application-solution.yml)

## Test Resources

### Lambda

Example Test Payload(s)

[kinesis-event](lambda/kinesis-event.json)

