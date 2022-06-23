# Firehose Transforming Delivery Stream

I keep going to this guide for help around roles, only because it is in cloudformation style

[cloudformation-example-for-kinesis-data-firehose-and-lambda](https://thomasstep.com/blog/cloudformation-example-for-kinesis-data-firehose-and-lambda)

[AWS: Building Lambda Functions with Java](https://docs.aws.amazon.com/lambda/latest/dg/lambda-java.html)

[StackOverflow: Lambda Kinesis Events](https://stackoverflow.com/questions/51710546/kinesis-firehose-data-transformation-using-java)

# Lambda Role

```yaml
# lambda-firehose-delivery-role
LambdaIamRole:
    Type: AWS::IAM::Role
    Properties:
      AssumeRolePolicyDocument:
        Version: 2012-10-17
        Statement:
            - Effect: Allow
              Principal:
                  Service:
                    - lambda.amazonaws.com
              Action:
                - sts:AssumeRole
      ManagedPolicyArns:
        - arn:aws:iam::aws:policy/service-role/AWSLambdaKinesisExecutionRole
  
LambdaFunction:
  Type: AWS::Lambda::Function
  Properties:
    Code:
      S3Bucket: !Ref SourceBucket
      S3Key: !Ref SourceKey
      S3ObjectVersion: !Ref SourceVersion
    Description: !Sub "Data transformation Lambda for Firehose Delivery Stream"
    Handler: com.j2clark.jstream.kinesis.function.LambdaKinesisTransform
    MemorySize: 512
    Role: !GetAtt LambdaIamRole.Arn
    Runtime: java11
    Timeout: 60

LambdaFunctionLogGroup:
  Type: AWS::Logs::LogGroup
  Properties:
    LogGroupName: !Sub "/aws/lambda/${LambdaFunction}"
    RetentionInDays: 7

LambdaFunctionPermission:
    Type: AWS::Lambda::Permission
    Properties:
      Action: lambda:InvokeFunction
      FunctionName: !GetAtt LambdaFunction.Arn
    Principal: events.amazonaws.com
```

Firehose DeliveryStream Policy (Used in DeliveryStream Creation)
```yaml
DeliveryStreamIamRole:
  Type: AWS::IAM::Role
  Properties:
    AssumeRolePolicyDocument:
      Version: 2012-10-17
      Statement:
        - Sid: ''
          Effect: Allow
          Principal:
            Service: firehose.amazonaws.com
          Action: 'sts:AssumeRole'
          Condition:
            StringEquals:
              'sts:ExternalId': !Ref 'AWS::AccountId'
  
DeliveryStreamPolicy:
  Type: AWS::IAM::Policy
  Properties:
    PolicyName: firehose-delivery-policy
    PolicyDocument:
      Version: 2012-10-17
      Statement:
        - Effect: Allow
          Action:
            - "glue:GetTable",
            - "glue:GetTableVersion",
            - "glue:GetTableVersions"
          Resource:
            - "arn:aws:glue:us-east-1:089600871681:catalog",
            - "arn:aws:glue:us-east-1:089600871681:database/*",
            - "arn:aws:glue:us-east-1:089600871681:table/*"
        - Effect: Allow
          Action:
            - "s3:AbortMultipartUpload",
            - "s3:GetBucketLocation",
            - "s3:GetObject",
            - "s3:ListBucket",
            - "s3:ListBucketMultipartUploads",
            - "s3:PutObject"
          Resource:
            - "arn:aws:s3:::firehose-deliverystream-test",
            - "arn:aws:s3:::firehose-deliverystream-test/*"
        - Effect: Allow
          Action:
            - "lambda:InvokeFunction",
            - "lambda:GetFunctionConfiguration"
          Resource:
            - "arn:aws:lambda:us-east-1:089600871681:function:KInesisDeliveryStreamTransform:$LATEST"
        - Effect: Allow
          Action:
            - "logs:PutLogEvents"
          Resource:
            - "arn:aws:logs:us-east-1:089600871681:log-group:/aws/kinesisfirehose/stocktrade-transform-firehose-deliverystream:log-stream:*",
#            - "arn:aws:logs:us-east-1:089600871681:log-group:%FIREHOSE_POLICY_TEMPLATE_PLACEHOLDER%:log-stream:*"
        - Effect: Allow
          Action:
            - "kinesis:DescribeStream"
            - "kinesis:GetShardIterator"
            - "kinesis:GetRecords"
            - "kinesis:ListShards"
          Resource: 
            - "arn:aws:kinesis:us-east-1:089600871681:stream/*"
```

### Example Kinesis Firehose Data Stream Source Event 
```json
{
  "invocationId": "invocationIdExample",
  "deliverySteamArn": "arn:aws:kinesis:EXAMPLE",
  "region": "us-east-1",
  "records": [
    {
      "recordId": "49546986683135544286507457936321625675700192471156785154",
      "approximateArrivalTimestamp": 1495072949453,
      "kinesisRecordMetadata": {
        "sequenceNumber": "49545115243490985018280067714973144582180062593244200961",
        "subsequenceNumber": "123456",
        "partitionKey": "partitionKey-03",
        "shardId": "shardId-000000000000",
        "approximateArrivalTimestamp": 1495072949453
      },
      "data": "SGVsbG8sIHRoaXMgaXMgYSB0ZXN0IDEyMy4="
    }
  ]
}
```

Firehose Delivery Stream Policy
```json
{
    "Version": "2012-10-17",
    "Statement": [
        {
            "Sid": "",
            "Effect": "Allow",
            "Action": [
                "glue:GetTable",
                "glue:GetTableVersion",
                "glue:GetTableVersions"
            ],
            "Resource": [
                "arn:aws:glue:us-east-1:089600871681:catalog",
                "arn:aws:glue:us-east-1:089600871681:database/%FIREHOSE_POLICY_TEMPLATE_PLACEHOLDER%",
                "arn:aws:glue:us-east-1:089600871681:table/%FIREHOSE_POLICY_TEMPLATE_PLACEHOLDER%/%FIREHOSE_POLICY_TEMPLATE_PLACEHOLDER%"
            ]
        },
        {
            "Sid": "",
            "Effect": "Allow",
            "Action": [
                "s3:AbortMultipartUpload",
                "s3:GetBucketLocation",
                "s3:GetObject",
                "s3:ListBucket",
                "s3:ListBucketMultipartUploads",
                "s3:PutObject"
            ],
            "Resource": [
                "arn:aws:s3:::firehose-deliverystream-test",
                "arn:aws:s3:::firehose-deliverystream-test/*"
            ]
        },
        {
            "Sid": "",
            "Effect": "Allow",
            "Action": [
                "lambda:InvokeFunction",
                "lambda:GetFunctionConfiguration"
            ],
            "Resource": "arn:aws:lambda:us-east-1:089600871681:function:KInesisDeliveryStreamTransform:$LATEST"
        },
        {
            "Sid": "",
            "Effect": "Allow",
            "Action": [
                "logs:PutLogEvents"
            ],
            "Resource": [
                "arn:aws:logs:us-east-1:089600871681:log-group:/aws/kinesisfirehose/stocktrade-transform-firehose-deliverystream:log-stream:*",
                "arn:aws:logs:us-east-1:089600871681:log-group:%FIREHOSE_POLICY_TEMPLATE_PLACEHOLDER%:log-stream:*"
            ]
        },
        {
            "Sid": "",
            "Effect": "Allow",
            "Action": [
                "kinesis:DescribeStream",
                "kinesis:GetShardIterator",
                "kinesis:GetRecords",
                "kinesis:ListShards"
            ],
            "Resource": "arn:aws:kinesis:us-east-1:089600871681:stream/%FIREHOSE_POLICY_TEMPLATE_PLACEHOLDER%"
        },
      {
        "Effect": "Allow",
        "Action": [
          "kms:GenerateDataKey",
          "kms:Decrypt"
        ],
        "Resource": [
          "arn:aws:kms:us-east-1:089600871681:key/%FIREHOSE_POLICY_TEMPLATE_PLACEHOLDER%"
        ],
        "Condition": {
          "StringEquals": {
            "kms:ViaService": "s3.us-east-1.amazonaws.com"
          },
          "StringLike": {
            "kms:EncryptionContext:aws:s3:arn": [
              "arn:aws:s3:::%FIREHOSE_POLICY_TEMPLATE_PLACEHOLDER%/*",
              "arn:aws:s3:::%FIREHOSE_POLICY_TEMPLATE_PLACEHOLDER%"
            ]
          }
        }
      },
      {
            "Effect": "Allow",
            "Action": [
                "kms:Decrypt"
            ],
            "Resource": [
                "arn:aws:kms:us-east-1:089600871681:key/%FIREHOSE_POLICY_TEMPLATE_PLACEHOLDER%"
            ],
            "Condition": {
                "StringEquals": {
                    "kms:ViaService": "kinesis.us-east-1.amazonaws.com"
                },
                "StringLike": {
                    "kms:EncryptionContext:aws:kinesis:arn": "arn:aws:kinesis:us-east-1:089600871681:stream/%FIREHOSE_POLICY_TEMPLATE_PLACEHOLDER%"
                }
            }
        }
    ]
}
```