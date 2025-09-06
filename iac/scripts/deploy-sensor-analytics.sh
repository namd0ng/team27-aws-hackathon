#!/bin/bash

# AlcoLook Sensor Analytics Infrastructure Deployment Script
# Supports CloudFormation, Terraform, and CDK

set -e

PROJECT_NAME="alcolook"
ENVIRONMENT="${1:-dev}"
DEPLOYMENT_TYPE="${2:-cloudformation}"

echo "🚀 Deploying AlcoLook Sensor Analytics Infrastructure"
echo "📊 Environment: $ENVIRONMENT"
echo "🛠️  Deployment Type: $DEPLOYMENT_TYPE"

case $DEPLOYMENT_TYPE in
  "cloudformation")
    echo "☁️  Deploying with CloudFormation..."
    aws cloudformation deploy \
      --template-file cloudformation/sensor-analytics-stack.yaml \
      --stack-name "$PROJECT_NAME-sensor-analytics-$ENVIRONMENT" \
      --parameter-overrides \
        Environment="$ENVIRONMENT" \
        ProjectName="$PROJECT_NAME" \
      --capabilities CAPABILITY_NAMED_IAM \
      --tags \
        Project="$PROJECT_NAME" \
        Environment="$ENVIRONMENT" \
        ManagedBy="CloudFormation"
    
    echo "✅ CloudFormation deployment completed!"
    
    # Get outputs
    aws cloudformation describe-stacks \
      --stack-name "$PROJECT_NAME-sensor-analytics-$ENVIRONMENT" \
      --query 'Stacks[0].Outputs' \
      --output table
    ;;
    
  "terraform")
    echo "🏗️  Deploying with Terraform..."
    cd terraform
    
    # Initialize if needed
    if [ ! -d ".terraform" ]; then
      terraform init
    fi
    
    # Plan and apply
    terraform plan \
      -var="environment=$ENVIRONMENT" \
      -var="project_name=$PROJECT_NAME" \
      -out="sensor-analytics.tfplan"
    
    terraform apply "sensor-analytics.tfplan"
    
    echo "✅ Terraform deployment completed!"
    
    # Show outputs
    terraform output
    cd ..
    ;;
    
  "cdk")
    echo "🔧 Deploying with CDK..."
    cd cdk
    
    # Install dependencies if needed
    if [ ! -d "node_modules" ]; then
      npm install
    fi
    
    # Bootstrap if needed (first time only)
    # cdk bootstrap
    
    # Deploy
    cdk deploy SensorAnalyticsStack \
      --context environment="$ENVIRONMENT" \
      --context projectName="$PROJECT_NAME" \
      --require-approval never
    
    echo "✅ CDK deployment completed!"
    cd ..
    ;;
    
  *)
    echo "❌ Invalid deployment type: $DEPLOYMENT_TYPE"
    echo "Valid options: cloudformation, terraform, cdk"
    exit 1
    ;;
esac

echo ""
echo "🎉 Sensor Analytics Infrastructure deployed successfully!"
echo "📈 Dashboard: https://console.aws.amazon.com/cloudwatch/home#dashboards:name=$PROJECT_NAME-sensor-analytics-$ENVIRONMENT"
echo "🪣 S3 Bucket: $PROJECT_NAME-sensor-analytics-$ENVIRONMENT"
echo ""
echo "📝 Next steps:"
echo "1. Configure Android app to send anonymized sensor data to S3"
echo "2. Monitor Lambda functions in CloudWatch"
echo "3. Review analytics results in the dashboard"
