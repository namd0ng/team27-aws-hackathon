# AlcoLook Infrastructure as Code (IaC)

이 문서는 AlcoLook 앱의 AWS 인프라를 코드로 관리하는 방법을 설명합니다.

## 🔒 개인정보 보호 우선 설계

**AlcoLook은 개인정보 보호를 최우선으로 설계되었습니다:**
- ❌ **이미지 저장 없음**: 얼굴 사진을 어디에도 저장하지 않음
- ✅ **실시간 분석**: 분석 후 즉시 삭제
- ✅ **익명화된 데이터**: 개인 식별 불가능한 통계만 저장

## 📁 디렉토리 구조

```
├── cloudformation/          # AWS CloudFormation 템플릿
│   └── alcolook-stack.yaml
├── terraform/              # Terraform 구성 파일
│   ├── main.tf
│   ├── variables.tf
│   └── outputs.tf
├── cdk/                    # AWS CDK (TypeScript)
│   ├── lib/
│   │   └── alcolook-stack.ts
│   ├── app.ts
│   ├── package.json
│   ├── tsconfig.json
│   └── cdk.json
└── deploy.sh              # 통합 배포 스크립트
```

## 🏗️ 관리되는 AWS 리소스

### 1. IAM Roles
- **Rekognition Service Role**: 실시간 얼굴 분석 권한 (읽기 전용)
- **Bedrock Service Role**: AI 모델 호출 권한

### 2. CloudWatch Logs
- **Application Log Group**: 익명화된 분석 결과만 저장 (30일 보관)

## 🚀 배포 방법

### 전제 조건
- AWS CLI 설치 및 구성
- 적절한 AWS 권한 (IAM, CloudWatch, Rekognition, Bedrock)

### 1. AWS CloudFormation
```bash
# 개발 환경 배포
./deploy.sh cloudformation dev

# 프로덕션 환경 배포
./deploy.sh cloudformation prod

# 수동 배포
aws cloudformation deploy \
  --template-file cloudformation/alcolook-stack.yaml \
  --stack-name alcolook-stack-dev \
  --parameter-overrides Environment=dev ProjectName=alcolook \
  --capabilities CAPABILITY_NAMED_IAM
```

### 2. Terraform
```bash
# 개발 환경 배포
./deploy.sh terraform dev

# 수동 배포
cd terraform
terraform init
terraform plan -var="environment=dev"
terraform apply
```

### 3. AWS CDK
```bash
# 개발 환경 배포
./deploy.sh cdk dev

# 수동 배포
cd cdk
npm install
npx cdk bootstrap
npx cdk deploy --context environment=dev
```

## 📋 환경별 설정

### 개발 환경 (dev)
- 리소스 이름: `alcolook-*-dev`
- 로그 보관: 30일
- 실시간 분석만

### 스테이징 환경 (staging)
- 리소스 이름: `alcolook-*-staging`
- 프로덕션과 동일한 설정

### 프로덕션 환경 (prod)
- 리소스 이름: `alcolook-*-prod`
- 강화된 보안 설정
- 모니터링 활성화

## 🔧 Android 앱 연동

배포 완료 후 다음 단계를 수행하세요:

### 1. 출력값 확인
```bash
# CloudFormation
aws cloudformation describe-stacks --stack-name alcolook-stack-dev --query 'Stacks[0].Outputs'

# Terraform
cd terraform && terraform output

# CDK
cd cdk && npx cdk list
```

### 2. AwsConfig.kt 업데이트
```kotlin
object AwsConfig {
    const val REGION = "us-east-1"
    const val REKOGNITION_ROLE_ARN = "arn:aws:iam::123456789012:role/alcolook-rekognition-role-dev"
    const val BEDROCK_ROLE_ARN = "arn:aws:iam::123456789012:role/alcolook-bedrock-role-dev"
    const val LOG_GROUP_NAME = "/aws/alcolook/dev"
    
    // 개인정보 보호 설정
    const val STORE_IMAGES = false  // 이미지 저장 금지
    const val ANONYMOUS_ONLY = true // 익명화된 데이터만
}
```

## 🗑️ 리소스 정리

### CloudFormation
```bash
aws cloudformation delete-stack --stack-name alcolook-stack-dev
```

### Terraform
```bash
cd terraform
terraform destroy -var="environment=dev"
```

### CDK
```bash
cd cdk
npx cdk destroy --context environment=dev
```

## 🔒 개인정보 보호 및 보안

### 1. 데이터 처리 원칙
- **최소 수집**: 분석에 필요한 데이터만
- **즉시 삭제**: 원본 이미지는 분석 후 즉시 삭제
- **익명화**: 개인 식별 불가능한 통계만 저장

### 2. 저장되는 데이터 예시
```json
{
  "timestamp": "2025-01-06T03:45:00Z",
  "analysis": {
    "eye_closure_ratio": 0.7,
    "mouth_opening_ratio": 0.3,
    "face_tilt_degrees": 15.2,
    "confidence_score": 0.85
  },
  "intoxication_level": "moderate"
}
```

### 3. 저장되지 않는 데이터
- ❌ 얼굴 이미지
- ❌ 개인 식별 정보
- ❌ 위치 정보
- ❌ 디바이스 정보

## 💰 비용 최적화

1. **S3 제거**: 스토리지 비용 완전 절약
2. **실시간 처리**: Rekognition 사용량 최소화
3. **CloudWatch Logs**: 30일 후 자동 삭제로 비용 관리
4. **리소스 태깅**: 비용 추적 및 관리 용이

## 🔍 모니터링 및 로깅

- **CloudWatch Logs**: 익명화된 분석 결과만 저장
- **AWS CloudTrail**: API 호출 추적 (별도 설정 필요)
- **Cost Explorer**: 비용 모니터링 (AWS 콘솔에서 확인)

## 🆘 문제 해결

### 일반적인 오류
1. **권한 부족**: IAM 정책 확인
2. **리소스 이름 충돌**: 환경별 고유 이름 사용
3. **리전 불일치**: 모든 리소스가 동일 리전에 있는지 확인

### 로그 확인
```bash
# CloudFormation 이벤트
aws cloudformation describe-stack-events --stack-name alcolook-stack-dev

# CloudWatch 로그
aws logs describe-log-groups --log-group-name-prefix "/aws/alcolook"
```

## 🎯 아키텍처 다이어그램

```
📱 Android App
    ↓ (이미지 직접 전송)
👁️ Amazon Rekognition (실시간 분석)
    ↓ (분석 결과만)
🤖 AWS Bedrock (AI 종합 분석)
    ↓ (익명 통계만)
📋 CloudWatch Logs
```

**핵심**: 개인정보는 저장하지 않고, 분석 결과만 활용하는 프라이버시 우선 아키텍처
