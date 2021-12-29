 ACCOUNT_ID=`aws sts get-caller-identity --output text --query Account`
 docker build . -f Dockerfile -t kinesis-producers/kinesis-producer
 docker tag kinesis-producers/kinesis-producer:latest ${ACCOUNT_ID}.dkr.ecr.ap-south-1.amazonaws.com/kinesis-consumers/kinesis-producer:latest
 docker push ${ACCOUNT_ID}.dkr.ecr.ap-south-1.amazonaws.com/kinesis-consumers/kinesis-producer:latest
