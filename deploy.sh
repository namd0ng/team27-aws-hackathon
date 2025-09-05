#!/bin/bash

# AlcoLook Infrastructure Deployment Script
# Usage: ./deploy.sh [cloudformation|terraform|cdk] [environment]

set -e

TOOL=${1:-cloudformation}
ENVIRONMENT=${2:-dev}
PROJECT_NAME="alcolook"

echo "🚀 Deploying AlcoLook infrastructure using $TOOL for $ENVIRONMENT environment..."

case $TOOL in
  "cloudformation")
    echo "📋 Deploying with AWS CloudFormation..."
    aws cloudformation deploy \
      --template-file cloudformation/alcolook-stack.yaml \
      --stack-name ${PROJECT_NAME}-stack-${ENVIRONMENT} \
      --parameter-overrides \
        Environment=${ENVIRONMENT} \
        ProjectName=${PROJECT_NAME} \
      --capabilities CAPABILITY_NAMED_IAM \
      --tags \
        Project=${PROJECT_NAME} \
        Environment=${ENVIRONMENT} \
        ManagedBy=CloudFormation
    
    echo "✅ CloudFormation deployment completed!"
    aws cloudformation describe-stacks \
      --stack-name ${PROJECT_NAME}-stack-${ENVIRONMENT} \
      --query 'Stacks[0].Outputs'
    ;;

  "terraform")
    echo "🏗️ Deploying with Terraform..."
    cd terraform
    
    # Initialize Terraform
    terraform init
    
    # Plan deployment
    terraform plan \
      -var="environment=${ENVIRONMENT}" \
      -var="project_name=${PROJECT_NAME}" \
      -out=tfplan
    
    # Apply deployment
    terraform apply tfplan
    
    echo "✅ Terraform deployment completed!"
    terraform output
    cd ..
    ;;

  "cdk")
    echo "☁️ Deploying with AWS CDK..."
    cd cdk
    
    # Install dependencies
    npm install
    
    # Bootstrap CDK (if needed)
    npx cdk bootstrap
    
    # Deploy stack
    npx cdk deploy \
      --context environment=${ENVIRONMENT} \
      --context projectName=${PROJECT_NAME} \
      --require-approval never
    
    echo "✅ CDK deployment completed!"
    npx cdk list
    cd ..
    ;;

  *)
    echo "❌ Unknown deployment tool: $TOOL"
    echo "Usage: ./deploy.sh [cloudformation|terraform|cdk] [environment]"
    exit 1
    ;;
esac

echo "🎉 Infrastructure deployment completed successfully!"
echo "📝 Next steps:"
echo "  1. Update AwsConfig.kt with the deployed resource ARNs"
echo "  2. Configure AWS credentials in your Android app"
echo "  3. Test the face analysis functionality"
