# AlcoLook - 다중 센서 기반 음주 측정 앱

**3가지 센서 데이터를 활용한 종합적인 음주 상태 분석 Android 애플리케이션**

## 🎯 주요 특징

### 📊 **다중 센서 측정 시스템**
1. **얼굴 인식 알고리즘** (필수)
   - Amazon Rekognition API 기반 얼굴 특징 분석
   - 실시간 카메라 촬영 또는 사진 업로드 지원
   - 눈 상태, 입 상태, 표정, 얼굴 기울기 종합 분석

2. **심박수 측정** (선택)
   - 헬스 커넥트를 통한 스마트워치 연동
   - 음주 상태에 따른 심박 변이도 분석
   - 워치 미착용 시 건너뛰기 가능

3. **자이로센서 측정** (선택)
   - 손 떨림 및 균형감각 측정
   - 흔들림 강도 및 안정성 점수 산출

### 🔄 **측정 플로우**
```
측정 방법 선택
├── 📷 카메라로 측정하기 → 실시간 얼굴 인식 → [기록하기]
└── 📁 사진 업로드하기 → 사진 얼굴 분석 → [다음으로]
                                    ↓
                            ❤️ 심박수 측정 (선택)
                                    ↓
                            📱 자이로센서 측정 (선택)
                                    ↓
                            📊 종합 분석 결과
```

## 🛠 기술 스택

### **Frontend**
- **Kotlin** + **Jetpack Compose** (Material 3)
- **Navigation Compose** (3-tab 구조)
- **CameraX** (v1.4.x) - 실시간 카메라 프리뷰
- **ML Kit Face Detection** - 얼굴 검출
- **Coil** - 이미지 로딩

### **Backend & AI**
- **Amazon Rekognition** - 얼굴 특징 분석
- **AWS Bedrock** - AI 기반 종합 분석 (구현 예정)
- **헬스 커넥트** - 스마트워치 심박수 연동

### **Infrastructure as Code (IaC)**
- **AWS CloudFormation** - AWS 리소스 자동 배포
- **Terraform** - 멀티 클라우드 인프라 관리
- **AWS CDK** - 프로그래밍 언어로 인프라 정의

### **Data & Storage**
- **Room Database** - 로컬 데이터 저장
- **Hilt** - 의존성 주입
- **완전 오프라인** - 네트워크 없이 동작

## 📱 화면 구성

### **1. 홈 (측정)**
- 측정 방법 선택 (카메라/사진 업로드)
- 실시간 얼굴 인식 및 분석 결과 표시
- 단계별 센서 측정 진행

### **2. 캘린더**
- **월별 탭**: 날짜별 음주 기록 및 상태 표시
- **통계 탭**: 주간/월간 요약, 건강 지수, 트렌드 분석

### **3. 설정**
- 사용자 프로필 관리
- 데이터 전체 삭제
- 도움말 및 면책 고지

## 🔬 분석 알고리즘

### **얼굴 분석 요소**
- **눈 상태**: 감김 정도 및 초점
- **입 상태**: 벌어짐 및 비대칭
- **표정 분석**: 혼란, 놀람, 혐오 등
- **얼굴 기울기**: Roll, Pitch, Yaw 각도
- **전체적 안면 대칭성**

### **심박수 분석**
- **BPM**: 분당 심박수 (정상: 60-90, 음주 시: 증가)
- **변이도**: 심박 간격의 불규칙성
- **측정 안정성**: 측정 품질 평가

### **자이로센서 분석**
- **흔들림 강도**: 0.0~1.0 (높을수록 불안정)
- **평균/최대 움직임**: 손 떨림 정도
- **안정성 점수**: 균형감각 평가

## 🎨 UI/UX 특징

### **Material 3 디자인**
- 다크/라이트 모드 지원
- 접근성 준수 (터치 타깃 44dp+, 대비 4.5:1+)
- 플랫폼 이모지 아이콘 사용

### **색상 시스템**
- `warning-soft = #FFF4E5` (주의 상태)
- `danger-soft = #FDEBEC` (폭음 상태)
- 상태별 직관적 컬러 코딩

### **사용자 경험**
- 단계별 가이드 제공
- 실시간 측정 상태 표시
- 선택적 센서 측정 (건너뛰기 가능)

## 🚀 설치 및 실행

### **요구사항**
- Android 8.0 (API 26) 이상
- 카메라 권한 필수
- 스마트워치 (심박수 측정 시)

### **빌드 방법**
```bash
git clone [repository-url]
cd team27-aws-hackathon
./gradlew assembleDebug
```

### **설정**
1. `AwsConfig.kt`에서 AWS 자격 증명 설정
2. 테스트 모드: `AwsConfig.TEST_MODE = true`
3. 카메라 권한 허용

## 🏗️ Infrastructure as Code (IaC)

### **AWS 리소스 관리**
본 프로젝트는 AWS 인프라를 코드로 관리하여 일관성 있고 재현 가능한 배포를 지원합니다.

#### **1. AWS CloudFormation**
```yaml
# cloudformation/rekognition-stack.yaml
Resources:
  RekognitionRole:
    Type: AWS::IAM::Role
    Properties:
      AssumeRolePolicyDocument:
        Statement:
          - Effect: Allow
            Principal:
              Service: rekognition.amazonaws.com
            Action: sts:AssumeRole
      
  BedrockEndpoint:
    Type: AWS::Bedrock::Agent
    Properties:
      AgentName: AlcoLookAnalyzer
      FoundationModel: anthropic.claude-3-sonnet
```

#### **2. Terraform**
```hcl
# terraform/main.tf
resource "aws_rekognition_collection" "face_collection" {
  collection_id = "alcolook-faces"
  
  tags = {
    Environment = "production"
    Project     = "AlcoLook"
  }
}

resource "aws_bedrock_agent" "analysis_agent" {
  agent_name         = "alcolook-analyzer"
  foundation_model   = "anthropic.claude-3-sonnet"
  instruction        = "Analyze intoxication levels based on sensor data"
}
```

#### **3. AWS CDK (TypeScript)**
```typescript
// cdk/lib/alcolook-stack.ts
import * as cdk from 'aws-cdk-lib';
import * as rekognition from 'aws-cdk-lib/aws-rekognition';
import * as bedrock from 'aws-cdk-lib/aws-bedrock';

export class AlcoLookStack extends cdk.Stack {
  constructor(scope: Construct, id: string, props?: cdk.StackProps) {
    super(scope, id, props);

    // Rekognition Collection
    const faceCollection = new rekognition.CfnCollection(this, 'FaceCollection', {
      collectionId: 'alcolook-faces'
    });

    // Bedrock Agent
    const analysisAgent = new bedrock.CfnAgent(this, 'AnalysisAgent', {
      agentName: 'alcolook-analyzer',
      foundationModel: 'anthropic.claude-3-sonnet'
    });
  }
}
```

### **배포 명령어**
```bash
# CloudFormation
aws cloudformation deploy --template-file cloudformation/rekognition-stack.yaml --stack-name alcolook-infra

# Terraform
cd terraform
terraform init
terraform plan
terraform apply

# AWS CDK
cd cdk
npm install
cdk bootstrap
cdk deploy
```

### **인프라 구성 요소**
- **Amazon Rekognition Collection** - 얼굴 분석용 컬렉션
- **AWS Bedrock Agent** - AI 기반 종합 분석 엔진
- **IAM Roles & Policies** - 최소 권한 원칙 적용
- **CloudWatch Logs** - 모니터링 및 로깅
- **S3 Bucket** - 임시 이미지 저장 (선택사항)

## 📊 데이터 모델

### **센서 데이터 구조**
```kotlin
// 얼굴 분석 결과
data class FaceAnalysisData(
    val confidence: Float,      // 신뢰도 (0.0~1.0)
    val eyesClosed: Boolean,    // 눈 감김 여부
    val mouthOpen: Boolean,     // 입 벌어짐 여부
    val faceAngle: Float        // 얼굴 기울기
)

// 심박수 데이터
data class HeartRateData(
    val bpm: Int,              // 분당 심박수
    val variability: Float,    // 심박 변이도
    val measurementDuration: Int // 측정 시간(초)
)

// 자이로센서 데이터
data class GyroscopeData(
    val shakingIntensity: Float,  // 흔들림 강도
    val stabilityScore: Float     // 안정성 점수
)
```

### **음주 상태 분류**
- **NORMAL**: 정상 (0-20%)
- **SLIGHTLY**: 조금 취함 (20-40%)
- **MODERATE**: 적당히 취함 (40-60%)
- **HEAVY**: 과음 (60%+)

## ⚠️ 면책 고지

**본 애플리케이션의 측정 결과는 의료 목적이 아니며, 운전 판단에 사용하지 마세요.**

- 엔터테인먼트 및 자가 인식 목적으로만 사용
- 의학적 진단이나 법적 증거로 사용 불가
- 음주 후 운전 여부는 반드시 전문 측정기구로 확인

## 🔄 개발 현황

### **✅ 완료된 기능**
- [x] 3-tab 네비게이션 구조
- [x] 얼굴 인식 측정 (카메라/사진)
- [x] 심박수 측정 UI (헬스 커넥트 연동)
- [x] 센서 데이터 모델 및 생성기
- [x] Material 3 디자인 시스템
- [x] 로컬 데이터 저장 구조

### **🚧 진행 중**
- [ ] 자이로센서 측정 화면
- [ ] 종합 분석 결과 화면
- [ ] 캘린더 데이터 연동
- [ ] Room 데이터베이스 구현

### **📋 예정 기능**
- [ ] AI 기반 종합 분석 (AWS Bedrock)
- [ ] 실제 헬스 커넥트 API 연동
- [ ] 통계 및 트렌드 분석
- [ ] 데이터 내보내기/가져오기

---

**Team 27 - AWS Hackathon 2025**
