# AlcoLook - 음주 측정 및 관리 앱

## 프로젝트 구조

### 기술 스택
- **Kotlin** + **Jetpack Compose** (Material 3)
- **Navigation Compose** for 3-tab navigation
- **Amazon Rekognition** for face analysis + **DynamoDB** for cloud storage
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
- DynamoDB 연동
- Hilt DI
- 실제 데이터 모델 및 비즈니스 로직