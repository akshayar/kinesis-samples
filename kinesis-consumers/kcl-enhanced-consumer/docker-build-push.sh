 docker build . -f Dockerfile -t kinesis-consumers/kcl-enhanced-consumer
 docker tag kinesis-consumers/kcl-enhanced-consumer:latest 799223504601.dkr.ecr.ap-south-1.amazonaws.com/kinesis-consumers/kcl-enhanced-consumer:latest
 docker push 799223504601.dkr.ecr.ap-south-1.amazonaws.com/kinesis-consumers/kcl-enhanced-consumer:latest
