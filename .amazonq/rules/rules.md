## 📐 Amazon Q Build Rules — AlcoLook (vNext, Calendar-Integrated Slim)

> 이 문서는 Amazon Q가 코드를 생성·수정할 때 반드시 준수해야 하는 규칙입니다. “권장(Should)”보다 강제(Must/Never) 규칙을 우선합니다.
> 

---

## 1) 스코프 & 비범위

- **Must**: 모든 기능은 **온디바이스(Local only)** 로 동작한다. **네트워크/계정/동기화 기능은 금지**.
- **Must**: 얼굴 분석은 **교체 가능한 인터페이스**(`IntoxicationPredictor`)로만 연결한다. 기본 구현은 **더미/룰 기반**.
- **Must**: 면책 문구(운전 판단 금지)를 **결과 화면에 상시 노출**.

---

## 2) 정보 구조(IA) & 내비게이션

- **Must**: 하단 탭은 **정확히 3개** — **홈(촬영) / 캘린더 / 설정**. (4개 금지)
- **Must**: **요약 탭은 존재하지 않는다**. 요약은 **캘린더 탭 내부 하위 탭**으로 통합한다.
- **Must**: **캘린더 탭**은 **하위 탭 2개**를 가진다 → **월별 / 통계**.
    - **통계** 내부는 **주간 요약 / 월간 요약** 토글을 제공한다(스샷과 시각 일치).
- **Must**: 설정은 상단 **오버플로(⋮)** 로 진입 가능해야 한다(프로필, 데이터 관리 등).

---

## 3) UI/UX 규칙

- **Must**: **Jetpack Compose(Material 3)** 를 사용한다. Pretendard 가능 시 적용, 미지원 시 시스템 폰트.
- **Must**: 색상 토큰 유지
    - `warning-soft = #FFF4E5`(주의), `danger-soft = #FDEBEC`(폭음)
    - 텍스트 대비 **WCAG 4.5:1 이상** 확보.
- **Must**: 스크린샷과 **≥90% 시각 일치**. 불가한 요소는 **가까운 머티리얼 컴포넌트**로 대체.
- **Must**: 이모지는 **플랫폼 이모지** 우선. 미적합 시 **Material Icons** 또는 **텍스트 라벨** 대체.
    - 매핑 예: 맥주🍺 / 소주🍶(또는 🥃) / 와인🍷 / 위스키🥃 / 하이볼🍹 / 칵테일🍸 / 막걸리🥛 / 기타⭐
- **Must**: 결과 화면 하단 액션은 **[공유] [결과 기록] [다시]** 순서를 지킨다.
- **Must**: 캘린더 DayCell은 상태에 따라 **중립/연주황/연적색** 배경(또는 도트)로 표시한다.
- **Should**: 다크 모드 대응(가능 범위 내).
- **Must**: 접근성 — 터치 타깃 ≥44dp, 콘텐츠 설명 라벨 제공.

---

## 4) 기능 규칙

### 4.1 얼굴 분석(홈)

- **Must**: **CameraX**(v1.4.x)로 촬영, **ML Kit 얼굴 검출**로 기본 피처 추출.
- **Must**: `IntoxicationPredictor` 인터페이스로 확률(0.0~1.0)을 반환. 기본 구현은 **룰 기반 더미**.
- **Must**: 얼굴 미검출/품질 저하 시 **재촬영 안내**. 다중 인물 시 **가장 큰 얼굴** 선택.
- **Must**: **결과 화면**에 확률%, 강아지 코멘트, 면책, 하단 3버튼을 표시.
- **Must**: **[결과 기록]** 누르면 다이어리 생성 시트로 **프리필** 후 저장 가능.

### 4.2 다이어리/캘린더

- **Must**: **CRUD 전부**(생성/조회/수정/삭제) + **Undo 삭제**(스낵바).
- **Must**: **월별** — 월 그리드, 날짜별 배지/상태, **선택일 기록 리스트** + **[요약 보기] 바텀시트**.
- **Must**: **통계** — **주간/월간** 토글, 캐릭터 코멘트 카드, **건강 지수(상태 배지+progress)**,
    - **주별 트렌드(막대)**, **종류별 통계(바/칩)**, **얼굴 분석 결과 카드(있을 때만)**.
- **Must**: 표준잔수 규칙 고정
    - `EtOH(g) = volume_ml × abv_% × 0.789` → `std_drinks = EtOH / 14`
- **Must**: 일일 상태 라벨(기본 프로필 기준, 단순화 규칙)
    - **양호**: ≤2잔 / **주의**: ≤5잔 / **폭음**: >5잔
    - 캘린더 표시: **주의=연주황** / **폭음=연적색**.
- **Should**: 캐릭터 코멘트(라이트 톤): 양호/주의/폭음별 1~2문장.

---

## 5) 데이터 모델(Room, 로컬)

- **Must**: Room 엔티티/DAO/리포지토리 구성.
- **DrinkType(enum)**: {BEER, SOJU, WINE, WHISKY, HIGHBALL, COCKTAIL, MAKGEOLLI, OTHER}
- **DrinkRecord(entity)**
    - `id: Long`, `date: LocalDate`, `type: DrinkType`, `abv: Float?`, `volumeMl: Int?`, `count: Int`, `note: String?`, `analysisProb: Float?`
- **DailySummary(calc/view)**
    - `date`, `totalMl`, `totalStdDrinks`, `status`
- **UserProfile(entity)**
    - `sex: {MALE,FEMALE,UNSET}`, `isSenior65: Boolean`, `weeklyGoalStdDrinks: Int?`
- **Must**: 모든 계산(표준잔수/상태)은 **로컬에서 즉시 반영**.

---

## 6) 권한·보안·정책

- **Must**: **카메라 권한만** 요청. `INTERNET` 권한 **추가 금지**.
- **Must**: 모든 데이터 **로컬 저장**. **데이터 전체 삭제**(설정 > 데이터 관리) 제공.
- **Must**: 면책 고지(“의료 목적 아님, **운전 판단 금지**”)를 결과 화면과 도움말에 **상시 표기**.

---

## 7) 기술 스택 & 구조

- **Must**: Kotlin, **Jetpack Compose(Material 3)**, **CameraX**(v1.4.x), **ML Kit(Face Detection)**, **Room**, **Hilt(DI)**.
- **Must**: **MVVM + Repository** 아키텍처. ViewModel은 UI State만 노출.
- **Must**: `IntoxicationPredictor` 를 DI 바인딩(기본=Dummy). 실제 모델 도입 시 **교체만**으로 동작해야 함.
- **Should**: strings.xml/색상 토큰/테마 분리, 패키지 계층(`ui/`, `data/`, `domain/`) 유지.

---

## 8) 차트 & 시각화

- **Must**: 통계 화면의 막대/바 표현은 **Compose만으로 구현**(Canvas/Box 조합 등).
    - 무거운 서드파티 차트 라이브러리 **금지**.
- **Must**: 축/눈금은 스크린샷 수준의 단순형. 애니메이션은 과하지 않게.

---

## 9) 에러/빈 상태/로딩

- **Must**: 각 화면에 **빈 상태 UI** 제공(예: “아직 기록이 없어요”).
- **Must**: 처리 중에는 **비차단 로딩 인디케이터**(Pull-to-refresh 불필요).
- **Must**: 예외 발생 시 **스낵바**로 피드백, 크래시 금지.

---

## 10) 빌드 & 품질 기준

- **Must**: 첫 촬영 → 결과 화면 전환 **< 500ms**(더미 기준).
- **Must**: 프레임 드랍 최소화(중첩 Recomposition 지양).
- **Must**: 모든 하드코드 텍스트는 **strings.xml**로 분리(기본 한국어).

---

## 11) 마일스톤의 DoD(Definition of Done)

- **M0**: 3탭·테마·캘린더 하위 탭(월별/통계) 전환 동작, 색상 토큰 반영.
- **M1**: 캘린더 월별 + 기록 **CRUD + Undo**, 선택일 **요약 바텀시트**, 상태 컬러 실시간 반영.
- **M2**: 통계(주간/월간) 카드·막대/바 구현, 얼굴 분석 결과 카드(데이터 있을 때만).
- **M3**: 촬영→더미 분석→결과 화면→**[결과 기록] 프리필 저장**까지 일련 흐름 무중단.

---

## 12) 참고(공식 문서)

- CameraX(Preview/Capture): https://developer.android.com/training/camerax
- ML Kit Face Detection: https://developers.google.com/ml-kit/vision/face-detection/android
- Jetpack Compose(Material 3): https://developer.android.com/jetpack/compose
- Room: https://developer.android.com/training/data-storage/room
- Hilt: https://developer.android.com/training/dependency-injection/hilt

> 위 규칙에서 벗어나는 구현은 자동 거절하고, 대체안을 제시한 뒤 PRD 규칙에 맞춰 재생성합니다.