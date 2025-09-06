#!/bin/bash

# AlcoLook - 전체 인프라 배포 스크립트
# 메인 앱 + 센서 분석 인프라를 한 번에 배포

set -e

PROJECT_NAME="alcolook"
ENVIRONMENT="${1:-dev}"
DEPLOYMENT_TYPE="${2:-cloudformation}"

echo "🚀 AlcoLook 전체 인프라 배포 시작"
echo "📊 Environment: $ENVIRONMENT"
echo "🛠️  Deployment Type: $DEPLOYMENT_TYPE"
echo ""

# 1. 메인 앱 인프라 배포
echo "1️⃣  메인 앱 인프라 배포 중..."
./deploy.sh "$ENVIRONMENT" "$DEPLOYMENT_TYPE"

echo ""

# 2. 센서 분석 인프라 배포
echo "2️⃣  센서 분석 인프라 배포 중..."
./deploy-sensor-analytics.sh "$ENVIRONMENT" "$DEPLOYMENT_TYPE"

echo ""
echo "🎉 전체 인프라 배포 완료!"
echo ""
echo "📋 배포된 리소스:"
echo "✅ Amazon Rekognition (얼굴 분석)"
echo "✅ AWS Bedrock (AI 분석)"
echo "✅ S3 Bucket (센서 데이터)"
echo "✅ Lambda Functions (데이터 분석)"
echo "✅ CloudWatch Dashboard (모니터링)"
echo ""
echo "🔗 유용한 링크:"
echo "📈 CloudWatch Dashboard: https://console.aws.amazon.com/cloudwatch/home#dashboards:name=$PROJECT_NAME-sensor-analytics-$ENVIRONMENT"
echo "🪣 S3 Console: https://console.aws.amazon.com/s3/buckets/$PROJECT_NAME-sensor-analytics-$ENVIRONMENT"
echo "⚡ Lambda Console: https://console.aws.amazon.com/lambda/home#/functions"
