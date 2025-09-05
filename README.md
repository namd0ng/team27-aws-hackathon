# AlcoLook - 다중 센서 기반 음주 측정 앱

AWS 해커톤 Team27 프로젝트

## 📱 프로젝트 개요

AlcoLook은 **3가지 센서 데이터를 활용한 종합적인 음주 상태 분석** Android 애플리케이션입니다.

### 🎯 주요 특징

  - **얼굴 분석**: 카메라를 통한 음주 상태 측정
  - **심박수 측정**: 스마트워치 연동을 통한 심박 변이도 분석 (선택)
  - **자이로센서 측정**: 손 떨림 및 균형감각 측정 (선택)
  - **음주 기록**: 일별/주별/월별 음주 패턴 분석 및 캘린더 시각화
  - **AI 분석**: AWS Bedrock을 활용한 종합 분석 (구현 예정)
  - **개인 프로필**: 개인 설정 및 목표 관리

-----

## 🛠 기술 스택

### Frontend (Android)

  - **Kotlin** + **Jetpack Compose** (Material 3)
  - **Navigation Compose** (3-tab 구조)
  - **CameraX** (v1.4.x) - 실시간 카메라 프리뷰
  - **ML Kit Face Detection** - 얼굴 검출
  - **Coil** - 이미지 로딩
  - **minSdk 26**, **compileSdk 36**, **targetSdk 36**

### Backend & AI

  - **Amazon Rekognition** - 얼굴 특징 분석
  - **AWS Bedrock** - AI 기반 종합 분석 (구현 예정)
  - **헬스 커넥트** - 스마트워치 심박수 연동
  - **DynamoDB** - 클라우드 데이터베이스
  - **AWS Lambda** - 백엔드 API (Python, Node.js)

### Infrastructure as Code (IaC)

  - **AWS CloudFormation** - AWS 리소스 자동 배포
  - **Terraform** - 멀티 클라우드 인프라 관리
  - **AWS CDK** - 프로그래밍 언어로 인프라 정의

### Data & Storage

  - **Room Database** - 로컬 데이터 저장
  - **Hilt** - 의존성 주입
  - **완전 오프라인** - 네트워크 없이 동작 (로컬 기능)

-----

## 🎨 디자인 및 UI/UX

### Material 3 디자인 시스템

  - 다크/라이트 모드 지원
  - 접근성 준수 (터치 타깃 44dp+, 대비 4.5:1+)
  - 플랫폼 이모지 아이콘 사용
  - **색상 토큰**: `warning-soft = #FFF4E5`, `danger-soft = #FDEBEC` 등 상태별 직관적 컬러 코딩

### 📱 화면 구성

1.  **홈 (측정)**: 측정 방법 선택, 실시간 얼굴 인식 및 단계별 센서 측정
2.  **캘린더**: 월별 음주 기록 및 상태 표시, 주간/월간 통계
3.  **설정**: 사용자 프로필 관리, 데이터 삭제, 도움말

### 사용자 경험

  - **단계별 가이드**: 사용자가 쉽게 따라 할 수 있도록 측정 과정을 안내합니다.
  - **실시간 측정 상태 표시**: 분석 진행 상태를 직관적으로 보여줍니다.
  - **선택적 센서 측정**: 심박수나 자이로센서 측정은 건너뛰기 가능합니다.

-----

## 🔬 분석 알고리즘 & 데이터 모델

### 다중 센서 측정 시스템

1.  **얼굴 인식**: Amazon Rekognition 기반의 얼굴 특징 분석 (눈 상태, 입 상태, 표정, 기울기 등)
2.  **심박수**: 헬스 커넥트를 통한 스마트워치 연동, 심박 변이도 분석
3.  **자이로센서**: 손 떨림 및 균형감각 측정, 흔들림 강도 및 안정성 점수 산출

### 센서 데이터 구조

  - `FaceAnalysisData`: 신뢰도, 눈/입 상태, 얼굴 기울기 등
  - `HeartRateData`: 분당 심박수(BPM), 심박 변이도 등
  - `GyroscopeData`: 흔들림 강도, 안정성 점수 등

### 음주 상태 분류

  - **NORMAL**: 정상 (0-20%)
  - **SLIGHTLY**: 조금 취함 (20-40%)
  - **MODERATE**: 적당히 취함 (40-60%)
  - **HEAVY**: 과음 (60%+)

### 📊 데이터베이스

  - **DynamoDB**: `alcolook-users` 및 `alcolook-user-profiles` 테이블
  - **Room Database**: 로컬 데이터 저장소

-----

## 🚀 시작하기

### 1\. Android 앱 빌드

```bash
git clone [repository-url]
cd team27-aws-hackathon
./gradlew assembleDebug
```

### 2\. AWS 백엔드 배포

  - **CloudFormation**: `aws cloudformation deploy --template-file cloudformation/rekognition-stack.yaml --stack-name alcolook-infra`
  - **Terraform**: `terraform apply`
  - **CDK**: `cdk deploy`

-----

## ⚠️ 면책 고지

**본 애플리케이션의 측정 결과는 의료 목적이 아니며, 운전 판단에 사용하지 마세요.**

  - 엔터테인먼트 및 자가 인식 목적으로만 사용
  - 의학적 진단이나 법적 증거로 사용 불가
  - 음주 후 운전 여부는 반드시 전문 측정기구로 확인

-----

**Team 27 아마존의 눈물 - AWS Q Developer Hackathon 2025**