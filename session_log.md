# AlcoLook 개발 세션 로그 - 2025-01-06

## 주요 작업 내용

### 1. 워크플로우 변경
- **기존**: 심박수 → 보행 테스트
- **변경**: 얼굴 분석 → 심박수 → 보행 테스트
- TestStep enum에 FACE_DETECTION 추가

### 2. 가중치 조정
- 보행 테스트: 85% (기존 70%에서 증가)
- 심박수: 15% (기존 30%에서 감소)
- 이유: 심박수는 커피, 스트레스 등 외부 요인 영향 많음

### 3. UI 개선
- 가중치 표시 제거 (사용자에게 노출 안함)
- 운전 판단 경고 문구 제거
- 진행 표시바 추가 (4단계: 25%, 50%, 75%, 100%)

### 4. 해결한 빌드 오류들
- **TOML 파일**: 중복된 [libraries], [plugins] 섹션 제거
- **themes.xml**: 중복된 XML 선언 제거
- **Theme.kt**: 파일 완전 재작성 (문법 오류 해결)
- **build.gradle.kts**: Guava 의존성 추가 (CameraX 호환성)
- **TestFlowScreen.kt**: FACE_DETECTION 분기 추가, faceResult 변수 추가

### 5. 현재 점수 시스템 분석
- **얼굴 분석**: 아직 최종 계산에 미반영 (별도 처리 필요)
- **심박수**: HeartRateStatus enum 기반 단계별 판정
- **보행**: WalkingTestResult enum 기반 우선 판정
- **계산 방식**: 숫자 점수가 아닌 if-else 우선순위 방식

### 6. 파일 수정 내역
- `libs.versions.toml`: 중복 제거
- `themes.xml`: XML 선언 통합
- `Theme.kt`: 완전 재작성
- `MainActivity.kt`: 중복 import 정리
- `TestFlowScreen.kt`: 워크플로우 변경, faceResult 추가
- `build.gradle.kts`: Guava 의존성 추가

### 7. 다음 작업 예정
- ResultScreen 함수 시그니처에 faceResult 파라미터 추가
- 얼굴 분석 결과를 최종 계산에 통합
- 실제 가중치 기반 점수 계산 시스템 구현

## 중요 설정
- **워크플로우**: 얼굴(1) → 심박수(2) → 보행(3) → 결과(4)
- **가중치**: 보행 85%, 심박수 15%
- **UI**: 가중치/경고 문구 숨김, 진행바 표시

## 빌드 상태
- 대부분의 컴파일 오류 해결 완료
- Guava 의존성 추가로 CameraX 호환성 확보
- Theme.kt 문법 오류 해결
