# AlcoLook Infrastructure as Code (IaC)

## 📁 폴더 구조

```
iac/
├── cloudformation/          # AWS CloudFormation 템플릿
│   ├── alcolook-stack.yaml         # 메인 앱 인프라
│   └── sensor-analytics-stack.yaml # 센서 분석 인프라
├── terraform/               # Terraform 구성
│   ├── main.tf                     # 메인 리소스
│   ├── sensor-analytics.tf         # 센서 분석 리소스
│   ├── variables.tf                # 변수 정의
│   └── outputs.tf                  # 출력 값
├── cdk/                     # AWS CDK (TypeScript)
│   ├── lib/
│   │   ├── alcolook-stack.ts       # 메인 스택
│   │   └── sensor-analytics-stack.ts # 센서 분석 스택
│   ├── app.ts                      # CDK 앱 진입점
│   ├── package.json                # 의존성
│   └── cdk.json                    # CDK 설정
├── scripts/                 # 배포 스크립트
│   ├── deploy.sh                   # 메인 배포
│   └── deploy-sensor-analytics.sh  # 센서 분석 배포
└── README.md               # 이 파일
```

## 🚀 빠른 시작

### 1. 메인 앱 인프라 배포

```bash
# CloudFormation
cd iac/scripts
./deploy.sh dev cloudformation

# Terraform  
./deploy.sh dev terraform

# CDK
./deploy.sh dev cdk
```

### 2. 센서 분석 인프라 배포

```bash
# CloudFormation
./deploy-sensor-analytics.sh dev cloudformation

# Terraform
./deploy-sensor-analytics.sh dev terraform

# CDK
./deploy-sensor-analytics.sh dev cdk
```

## 📊 인프라 구성 요소

### 메인 앱 인프라
- **Amazon Rekognition**: 실시간 얼굴 분석
- **AWS Bedrock**: AI 기반 종합 분석
- **IAM Roles**: 최소 권한 원칙
- **CloudWatch Logs**: 익명화된 로그

### 센서 분석 인프라
- **S3 Bucket**: 익명화된 센서 데이터 저장
- **Lambda Functions**: 자이로스코프/심박수 분석
- **CloudWatch Dashboard**: 실시간 모니터링
- **자동 삭제**: 30일 후 데이터 자동 삭제

## 🔧 환경별 배포

### Development
```bash
./deploy.sh dev cloudformation
./deploy-sensor-analytics.sh dev cloudformation
```

### Staging
```bash
./deploy.sh staging terraform
./deploy-sensor-analytics.sh staging terraform
```

### Production
```bash
./deploy.sh prod cdk
./deploy-sensor-analytics.sh prod cdk
```

## 💰 비용 예상

| 환경 | 월간 예상 비용 |
|------|---------------|
| Dev | ~$5 |
| Staging | ~$10 |
| Production | ~$20 |

## 🔒 보안 고려사항

- ✅ 개인정보 저장 없음
- ✅ 최소 권한 IAM 역할
- ✅ 데이터 자동 삭제 (30일)
- ✅ 완전 비공개 S3 버킷
- ✅ 암호화 저장

## 📈 모니터링

### CloudWatch 대시보드
- Lambda 실행 메트릭
- 오류율 및 지연시간
- S3 스토리지 사용량

### 알람 설정
- 오류율 5% 초과
- Lambda 실행 시간 30초 초과
- S3 용량 1GB 초과

## 🛠️ 개발자 가이드

### 새 리소스 추가
1. 해당 IaC 도구 폴더에 리소스 정의
2. 스크립트에 배포 로직 추가
3. README 업데이트

### 환경 변수
```bash
export AWS_REGION=us-east-1
export PROJECT_NAME=alcolook
export ENVIRONMENT=dev
```

### 의존성 설치
```bash
# Terraform
cd terraform && terraform init

# CDK
cd cdk && npm install
```

## 🔄 CI/CD 통합

### GitHub Actions 예시
```yaml
name: Deploy Infrastructure
on:
  push:
    branches: [main]
    paths: ['iac/**']

jobs:
  deploy:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v3
      - name: Deploy with Terraform
        run: |
          cd iac/scripts
          ./deploy.sh prod terraform
```

## 📞 지원

문제 발생 시:
1. `iac/scripts/` 폴더의 로그 확인
2. AWS CloudWatch 콘솔 점검
3. GitHub Issues에 문의

## 📚 참고 문서

- [AWS CloudFormation 가이드](https://docs.aws.amazon.com/cloudformation/)
- [Terraform AWS Provider](https://registry.terraform.io/providers/hashicorp/aws/)
- [AWS CDK 개발자 가이드](https://docs.aws.amazon.com/cdk/)
