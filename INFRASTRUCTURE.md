# AlcoLook Infrastructure as Code (IaC)

이 문서는 AlcoLook 앱의 AWS 인프라를 코드로 관리하는 방법을 설명합니다.

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
- **Rekognition Service Role**: 얼굴 분석 권한
- **Bedrock Service Role**: AI 모델 호출 권한

### 2. S3 Bucket
- **Image Storage**: 임시 이미지 저장 (1일 후 자동 삭제)
- **Public Access Block**: 보안을 위한 퍼블릭 액세스 차단

### 3. CloudWatch Logs
- **Application Log Group**: 앱 로그 수집 (30일 보관)

## 🚀 배포 방법

### 전제 조건
- AWS CLI 설치 및 구성
- 적절한 AWS 권한 (IAM, S3, CloudWatch, Rekognition, Bedrock)

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
- S3 객체 만료: 1일

### 스테이징 환경 (staging)
- 리소스 이름: `alcolook-*-staging`
- 프로덕션과 동일한 설정

### 프로덕션 환경 (prod)
- 리소스 이름: `alcolook-*-prod`
- 강화된 보안 설정
- 백업 및 모니터링 활성화

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
    const val IMAGE_BUCKET_NAME = "alcolook-images-dev-123456789012"
    const val LOG_GROUP_NAME = "/aws/alcolook/dev"
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

## 🔒 보안 고려사항

1. **최소 권한 원칙**: IAM 역할은 필요한 최소 권한만 부여
2. **S3 보안**: 퍼블릭 액세스 완전 차단
3. **데이터 보관**: 임시 이미지는 1일 후 자동 삭제
4. **로그 관리**: CloudWatch 로그는 30일 후 자동 삭제
5. **태그 관리**: 모든 리소스에 프로젝트 및 환경 태그 적용

## 📊 비용 최적화

1. **S3 Lifecycle**: 임시 파일 자동 삭제로 스토리지 비용 절약
2. **CloudWatch Logs**: 로그 보관 기간 제한으로 비용 관리
3. **리소스 태깅**: 비용 추적 및 관리 용이
4. **환경별 분리**: 개발/스테이징/프로덕션 환경 독립 관리

## 🔍 모니터링 및 로깅

- **CloudWatch Logs**: 애플리케이션 로그 중앙 집중 관리
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
