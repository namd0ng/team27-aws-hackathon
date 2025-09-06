#!/bin/bash

# 간단한 배포 스크립트 (pip 의존성 없이)

set -e

echo "🚀 AlcoLook Backend 배포를 시작합니다..."

# 변수 설정
STACK_NAME="alcolook-backend"
REGION="us-east-1"  # 버지니아 북부 리전
JWT_SECRET="your-super-secret-jwt-key-change-this-in-production"

# 1. CloudFormation 스택 배포
echo "📦 CloudFormation 스택 배포 중..."
aws cloudformation deploy \
    --template-file cloudformation/alcolook-infrastructure.yaml \
    --stack-name $STACK_NAME \
    --parameter-overrides JWTSecret=$JWT_SECRET \
    --capabilities CAPABILITY_IAM \
    --region $REGION

# 2. Lambda 함수 코드 패키징 및 배포 (의존성 없이)
echo "📝 Lambda 함수 배포 중..."

# 임시 디렉토리 생성
mkdir -p temp

# 각 Lambda 함수 배포
for func in user_register user_login forgot_password; do
    echo "  - $func 함수 배포 중..."
    
    # 패키지 디렉토리 생성
    mkdir -p temp/$func
    
    # 함수 코드만 복사 (의존성은 Lambda 런타임에서 제공)
    cp lambda/${func}.py temp/$func/
    
    # ZIP 파일 생성
    cd temp/$func
    zip -r ../${func}.zip .
    cd ../..
    
    # Lambda 함수 업데이트
    aws lambda update-function-code \
        --function-name alcolook-${func//_/-} \
        --zip-file fileb://temp/${func}.zip \
        --region $REGION
done

# 3. API Gateway URL 출력
echo "🌐 API Gateway URL 가져오는 중..."
API_URL=$(aws cloudformation describe-stacks \
    --stack-name $STACK_NAME \
    --query 'Stacks[0].Outputs[?OutputKey==`ApiGatewayUrl`].OutputValue' \
    --output text \
    --region $REGION)

echo "✅ 배포 완료!"
echo "📍 API Gateway URL: $API_URL"
echo ""
echo "🔧 Android 앱에서 사용할 URL:"
echo "   NetworkModule.kt의 BASE_URL을 다음으로 변경하세요:"
echo "   private const val BASE_URL = \"$API_URL/\""

# 임시 파일 정리
rm -rf temp

echo ""
echo "🎉 AlcoLook Backend 배포가 완료되었습니다!"
