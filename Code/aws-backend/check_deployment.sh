#!/bin/bash

# 배포 상태 확인 스크립트

STACK_NAME="alcolook-backend"
REGION="us-east-1"

echo "🔍 CloudFormation 스택 상태 확인..."
aws cloudformation describe-stacks \
    --stack-name $STACK_NAME \
    --region $REGION \
    --query 'Stacks[0].StackStatus' \
    --output text

echo ""
echo "📊 DynamoDB 테이블 확인..."
aws dynamodb list-tables \
    --region $REGION \
    --query 'TableNames[?contains(@, `alcolook`)]'

echo ""
echo "🌐 API Gateway URL:"
aws cloudformation describe-stacks \
    --stack-name $STACK_NAME \
    --query 'Stacks[0].Outputs[?OutputKey==`ApiGatewayUrl`].OutputValue' \
    --output text \
    --region $REGION

echo ""
echo "👥 사용자 테이블 데이터 확인:"
aws dynamodb scan \
    --table-name alcolook-users \
    --region $REGION \
    --query 'Items[].{Email:email.S,Name:name.S,CreatedAt:created_at.S}' \
    --output table
