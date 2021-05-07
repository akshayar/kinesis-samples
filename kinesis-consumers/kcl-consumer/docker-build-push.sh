 docker build . -f Dockerfile -t kinesis-consumers/kcl-consumer
 docker tag kinesis-consumers/kcl-consumer:latest 799223504601.dkr.ecr.ap-south-1.amazonaws.com/kinesis-consumers/kcl-consumer:latest
 docker push 799223504601.dkr.ecr.ap-south-1.amazonaws.com/kinesis-consumers/kcl-consumer:latest
