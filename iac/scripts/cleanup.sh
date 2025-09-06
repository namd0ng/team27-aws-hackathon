#!/bin/bash

# AlcoLook - 인프라 정리 스크립트
# 모든 AWS 리소스를 안전하게 삭제

set -e

PROJECT_NAME="alcolook"
ENVIRONMENT="${1:-dev}"
DEPLOYMENT_TYPE="${2:-cloudformation}"

echo "🧹 AlcoLook 인프라 정리 시작"
echo "📊 Environment: $ENVIRONMENT"
echo "🛠️  Deployment Type: $DEPLOYMENT_TYPE"
echo ""

read -p "⚠️  정말로 '$ENVIRONMENT' 환경의 모든 리소스를 삭제하시겠습니까? (yes/no): " confirm

if [ "$confirm" != "yes" ]; then
    echo "❌ 취소되었습니다."
    exit 1
fi

case $DEPLOYMENT_TYPE in
  "cloudformation")
    echo "☁️  CloudFormation 스택 삭제 중..."
    
    # 센서 분석 스택 삭제
    aws cloudformation delete-stack \
      --stack-name "$PROJECT_NAME-sensor-analytics-$ENVIRONMENT" || true
    
    # 메인 스택 삭제
    aws cloudformation delete-stack \
      --stack-name "$PROJECT_NAME-$ENVIRONMENT" || true
    
    echo "⏳ 스택 삭제 대기 중..."
    aws cloudformation wait stack-delete-complete \
      --stack-name "$PROJECT_NAME-sensor-analytics-$ENVIRONMENT" || true
    aws cloudformation wait stack-delete-complete \
      --stack-name "$PROJECT_NAME-$ENVIRONMENT" || true
    ;;
    
  "terraform")
    echo "🏗️  Terraform 리소스 삭제 중..."
    cd ../terraform
    
    terraform destroy \
      -var="environment=$ENVIRONMENT" \
      -var="project_name=$PROJECT_NAME" \
      -auto-approve
    
    cd ../scripts
    ;;
    
  "cdk")
    echo "🔧 CDK 스택 삭제 중..."
    cd ../cdk
    
    cdk destroy \
      --context environment="$ENVIRONMENT" \
      --context projectName="$PROJECT_NAME" \
      --force
    
    cd ../scripts
    ;;
esac

echo ""
echo "✅ 인프라 정리 완료!"
echo "🗑️  삭제된 리소스:"
echo "   - IAM Roles"
echo "   - S3 Buckets"
echo "   - Lambda Functions"
echo "   - CloudWatch Dashboards"
echo "   - CloudWatch Log Groups"
