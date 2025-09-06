#!/bin/bash

# AlcoLook Profile API 배포 스크립트

REGION="ap-northeast-2"
STACK_NAME="alcolook-profile-api"

echo "🚀 AlcoLook Profile API 배포 시작..."

# 1. CloudFormation 스택 배포
echo "📦 CloudFormation 스택 배포 중..."
aws cloudformation deploy \
  --template-file cloudformation/profile-table.yaml \
  --stack-name $STACK_NAME \
  --capabilities CAPABILITY_IAM \
  --region $REGION

if [ $? -ne 0 ]; then
  echo "❌ CloudFormation 배포 실패"
  exit 1
fi

# 2. Lambda 함수 코드 업데이트
echo "🔄 Lambda 함수 코드 업데이트 중..."
cd lambda
zip -r profile-api.zip profile-api.js node_modules/ 2>/dev/null || zip profile-api.zip profile-api.js

aws lambda update-function-code \
  --function-name alcolook-profile-api \
  --zip-file fileb://profile-api.zip \
  --region $REGION

if [ $? -ne 0 ]; then
  echo "❌ Lambda 코드 업데이트 실패"
  exit 1
fi

# 3. API 엔드포인트 출력
echo "✅ 배포 완료!"
echo "📡 API 엔드포인트:"
aws cloudformation describe-stacks \
  --stack-name $STACK_NAME \
  --region $REGION \
  --query 'Stacks[0].Outputs[?OutputKey==`ApiEndpoint`].OutputValue' \
  --output text

echo ""
echo "🔗 API 사용법:"
echo "GET    /profile/{userId} - 프로필 조회"
echo "POST   /profile/{userId} - 프로필 생성"
echo "PUT    /profile/{userId} - 프로필 수정"
echo "DELETE /profile/{userId} - 프로필 삭제"
