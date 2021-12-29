 ACCOUNT_ID=`aws sts get-caller-identity --output text --query Account`
 docker build . -f Dockerfile -t kinesis-consumers/kcl-enhanced-consumer
 docker tag kinesis-consumers/kcl-enhanced-consumer:latest ${ACCOUNT_ID}.dkr.ecr.ap-south-1.amazonaws.com/kinesis-consumers/kcl-enhanced-consumer:latest
 docker push ${ACCOUNT_ID}.dkr.ecr.ap-south-1.amazonaws.com/kinesis-consumers/kcl-enhanced-consumer:latest
