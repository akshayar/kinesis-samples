aws ecs run-task \
        --cluster fargate-cluster \
        --task-definition kcl-consumer \
        --launch-type FARGATE 
        --network-configuration "awsvpcConfiguration={subnets=['subnet-0407b1173827ed96a','subnet-12345'],securityGroups=['test'],assignPublicIp='DISABLED'}"