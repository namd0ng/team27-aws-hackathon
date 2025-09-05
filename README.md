# AlcoLook - 음주 감지 앱

Amazon Rekognition을 활용한 얼굴 인식 기반 음주 감지 Android 애플리케이션

## 기능

- 실시간 카메라를 통한 얼굴 인식
- Amazon Rekognition API를 활용한 얼굴 특징 분석
- 음주 정도를 퍼센트로 표시
- 취함 정도에 따른 메시지 제공

## 분석 요소

- **눈 상태**: 눈이 감겨있거나 반쯤 감긴 정도
- **입 상태**: 입이 벌어져 있는 정도
- **표정 분석**: 혼란, 놀람, 혐오 등의 표정
- **얼굴 기울기**: Roll, Pitch, Yaw 각도 분석

## 설치 및 실행

1. Android Studio에서 프로젝트 열기
2. `AwsConfig.kt`에서 AWS 자격 증명 설정 (또는 테스트 모드 사용)
3. 앱 빌드 및 실행
4. 카메라 권한 허용

## 테스트 모드

실제 AWS 자격 증명 없이도 테스트 가능:
- `AwsConfig.TEST_MODE = true`로 설정
- 랜덤 값으로 음주 감지 결과 시뮬레이션
- 
# AlcoLook - 음주 측정 및 관리 앱

## 프로젝트 구조

### 기술 스택
- **Kotlin** + **Jetpack Compose** (Material 3)
- **Navigation Compose** for 3-tab navigation
- **Local-only** data storage (no network)
- **minSdk 26**, **compileSdk 36**, **targetSdk 36**

### 아키텍처
```
com.hackathon.alcolook/
├── ui/
│   ├── theme/           # Material 3 테마 (Color, Theme, Type)
│   ├── navigation/      # 네비게이션 구성
│   ├── home/           # 홈 화면 (얼굴 분석/촬영)
│   ├── calendar/       # 캘린더 화면 (월별/통계 하위 탭)
│   ├── settings/       # 설정 화면
│   └── components/     # 공용 컴포넌트
└── MainActivity.kt
```

### 주요 기능
1. **홈 (촬영)**: 얼굴 분석 및 촬영 인터페이스
2. **캘린더**: 
   - 월별 탭: 월 그리드 + 날짜별 상태 표시
   - 통계 탭: 주간/월간 요약, 건강 지수, 트렌드
3. **설정**: 프로필, 데이터 관리, 도움말

### 색상 토큰 (PRD 준수)
- `warning-soft = #FFF4E5` (주의 상태)
- `danger-soft = #FDEBEC` (폭음 상태)

### 특징
- **XML 테마 없음**: Compose Material 3만 사용
- **ActionBar 비활성화**: `WindowCompat.setDecorFitsSystemWindows(window, false)`
- **이모지 아이콘**: Material Icons 대신 플랫폼 이모지 사용
- **한국어 UI**: strings.xml에 모든 텍스트 정의
- **면책 고지**: 결과 화면과 설정에 상시 표기

### 빌드 및 실행
```bash
./gradlew assembleDebug
```

### 다음 단계 (구현 예정)
- CameraX 통합
- ML Kit 얼굴 검출
- Room 데이터베이스
- Hilt DI
- 실제 데이터 모델 및 비즈니스 로직