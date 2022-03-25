## Kinesis Consumer Lambda Sample

### Build
```shell
mvn clean install -DskipTests
```
### Deploy
```shell
export BUCKET_NAME=<>
export REGION=<>
export KinesisStreamName=<>
aws s3 cp target/kinesis-consumer-lambda-1.0.0.jar s3://${BUCKET_NAME}/learning/kinesis/lambda/kinesis-consumer-lambda-1.0.0.jar
aws cloudformation deploy \
--template-file src/main/aws/lambda-no-stream-no-vpc.yaml \
--stack kinesis-lambda \
--capabilities CAPABILITY_NAMED_IAM \
--region ${REGION} \
--parameter-overrides DeployBucket=${BUCKET_NAME} KinesisStreamName=${KinesisStreamName}

```

### Update Code
```shell
aws lambda update-function-code --function-name learning-kinesis-dev-lambda --region ${REGION} --zip-file fileb://./target/kinesis-consumer-lambda-1.0.0.jar

```