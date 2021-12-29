 ACCOUNT_ID=`aws sts get-caller-identity --output text --query Account`
 docker build . -f Dockerfile -t kinesis-consumers/kcl-consumer
 docker tag kinesis-consumers/kcl-consumer:latest ${ACCOUNT_ID}.dkr.ecr.us-east-1.amazonaws.com/kinesis-consumers/kcl-consumer:latest
 docker push ${ACCOUNT_ID}.dkr.ecr.us-east-1.amazonaws.com/kinesis-consumers/kcl-consumer:latest
