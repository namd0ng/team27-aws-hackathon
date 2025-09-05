# AlcoLook - 음주 측정 및 관리 앱

AWS 해커톤 Team27 프로젝트

## 📱 프로젝트 개요

AlcoLook은 얼굴 분석을 통한 음주 측정 및 관리 앱입니다.

### 주요 기능
- 📸 **얼굴 분석**: 카메라를 통한 음주 상태 측정
- 📊 **음주 기록**: 일별/주별/월별 음주 패턴 분석
- 📅 **캘린더**: 음주 기록 시각화 및 관리
- 👤 **프로필**: 개인 설정 및 목표 관리

## 🏗️ 기술 스택

### Frontend (Android)
- **Kotlin** + **Jetpack Compose** (Material 3)
- **Navigation Compose** (3-tab navigation)
- **CameraX** (카메라 기능)
- **ML Kit** (얼굴 검출)
- **OkHttp** (네트워크 통신)

### Backend (AWS)
- **AWS Lambda** (Python + Node.js)
- **Amazon DynamoDB** (데이터 저장)
- **API Gateway** (REST API)
- **JWT** 인증

## 📁 프로젝트 구조

```
team27-aws-hackathon/
├── app/                    # Android 앱
│   ├── src/main/java/com/hackathon/alcolook/
│   │   ├── ui/            # UI 컴포넌트
│   │   │   ├── home/      # 홈 화면 (촬영)
│   │   │   ├── calendar/  # 캘린더 화면
│   │   │   ├── settings/  # 설정 화면
│   │   │   └── auth/      # 로그인/회원가입
│   │   ├── data/          # 데이터 레이어
│   │   └── theme/         # Material 3 테마
│   └── build.gradle.kts
├── aws-backend/           # AWS Lambda 함수 (Python)
│   ├── lambda/
│   │   ├── user_login.py     # 로그인 API
│   │   ├── user_register.py  # 회원가입 API
│   │   └── update_profile.py # 프로필 업데이트
│   └── cloudformation/       # AWS 인프라
├── backend/               # 프로필 전용 API (Node.js)
│   ├── lambda/
│   │   └── profile-api.js    # 프로필 CRUD
│   └── cloudformation/
└── docs/                  # 문서
    └── PRD.md            # 제품 요구사항 문서
```

## 🚀 시작하기

### 1. Android 앱 빌드
```bash
./gradlew assembleDebug
```

### 2. AWS 백엔드 배포
```bash
cd aws-backend
./deploy.sh
```

### 3. 프로필 API 배포
```bash
cd backend
./deploy.sh
```

## 🎨 디자인 시스템

### 색상 토큰
- `warning-soft = #FFF4E5` (주의 상태)
- `danger-soft = #FDEBEC` (폭음 상태)

### 네비게이션
- **홈**: 얼굴 분석 및 촬영
- **캘린더**: 월별 기록 + 통계 (하위 탭)
- **설정**: 프로필, 데이터 관리, 도움말

## 📊 데이터베이스

### DynamoDB 테이블
1. **alcolook-users**: 사용자 정보
2. **alcolook-user-profiles**: 사용자 프로필

### 데이터 모델
```json
{
  "userId": "string",
  "sex": "MALE|FEMALE|UNSET",
  "age": "number",
  "isSenior65": "boolean",
  "weeklyGoalStdDrinks": "number"
}
```

## 🔐 인증

- JWT 토큰 기반 인증
- 7일 만료 정책
- 로그인/회원가입/비밀번호 찾기 지원

## ⚠️ 면책 고지

이 앱은 의료 목적이 아니며, 운전 판단에 사용하지 마세요.
모든 데이터는 개인 정보 보호를 위해 안전하게 관리됩니다.

## 👥 Team27

AWS 해커톤 참가팀
- 음주 측정 및 관리 솔루션 개발
- DynamoDB 기반 클라우드 아키텍처
