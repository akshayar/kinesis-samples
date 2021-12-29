ACCOUNT_ID=`aws sts get-caller-identity --output text --query Account`
sed "s/ACCOUNT_ID/${ACCOUNT_ID}/g" task-definition.json > out-task-definition.json
aws ecs register-task-definition --cli-input-json file://out-task-definition.json
