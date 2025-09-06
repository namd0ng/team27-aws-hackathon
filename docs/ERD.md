# AlcoLook ERD (Entity Relationship Diagram)

## 데이터베이스 구조 개요

AlcoLook은 **AWS DynamoDB**를 사용한 클라우드 기반 데이터 저장 구조를 사용합니다.

**📋 테이블별 역할:**
- **Users** → 회원가입/로그인 (이메일, 비밀번호, 인증)
- **UserProfiles** → 개인 프로필 설정 (성별, 나이, 주간 목표)
- **DrinkRecords** → 캘린더 월별 개별 음주 기록 (각각의 술 기록)
- **DailySummary** → 캘린더 통계 데이터 (일별 집계, 상태 표시)
- **IntoxicationTests** → 취함 정도 측정 데이터 (얼굴분석, 심박수, 자이로스코프)

---

## 1. AWS DynamoDB 테이블

### 1.1 Users (회원가입/로그인)
```
┌─────────────────────────────────────┐
│              Users                  │
├─────────────────────────────────────┤
│ PK  user_id: String                 │
│     email: String                   │
│     password_hash: String           │
│     created_at: String (ISO)        │
│     updated_at: String (ISO)        │
│     last_login: String (ISO)?       │
│     is_active: Boolean              │
└─────────────────────────────────────┘
```

### 1.2 UserProfiles (개인 프로필 설정)
```
┌─────────────────────────────────────┐
│           UserProfiles              │
├─────────────────────────────────────┤
│ PK  user_id: String                 │
│     sex: String                     │
│     age: Number?                    │
│     isSenior65: Boolean             │
│     weeklyGoalStdDrinks: Number?    │
│     created_at: String (ISO)        │
│     updated_at: String (ISO)        │
└─────────────────────────────────────┘
```

### 1.3 DrinkRecords (캘린더 월별 개별 음주 기록)
```
┌─────────────────────────────────────┐
│           DrinkRecords              │
├─────────────────────────────────────┤
│ PK  user_id: String                 │
│ SK  record_id: String               │
│     date: String (YYYY-MM-DD)       │
│     type: String                    │
│     abv: Number?                    │
│     volume_ml: Number?              │
│     count: Number                   │
│     note: String?                   │
│     analysis_prob: Number?          │
│     created_at: String (ISO)        │
│     updated_at: String (ISO)        │
│     device_id: String?              │
└─────────────────────────────────────┘
```

### 1.4 DailySummary (캘린더 통계 데이터)
```
┌─────────────────────────────────────┐
│           DailySummary              │
├─────────────────────────────────────┤
│ PK  user_id: String                 │
│ SK  date: String (YYYY-MM-DD)       │
│     total_volume_ml: Number         │
│     total_std_drinks: Number        │
│     status: String                  │
│     record_count: Number            │
│     has_analysis_data: Boolean      │
│     avg_intoxication_level: Number? │
│     max_intoxication_level: Number? │
│     intoxication_test_count: Number │
│     created_at: String (ISO)        │
│     updated_at: String (ISO)        │
└─────────────────────────────────────┘
```

### 1.5 IntoxicationTests (취함 정도 측정)
```
┌─────────────────────────────────────┐
│        IntoxicationTests            │
├─────────────────────────────────────┤
│ PK  user_id: String                 │
│ SK  test_id: String                 │
│     date: String (YYYY-MM-DD)       │
│     timestamp: String (ISO)         │
│     face_analysis_result: String    │
│     heart_rate: Number?             │
│     heart_rate_variability: Number? │
│     gyroscope_stability: Number?    │
│     gyroscope_reaction_time: Number?│
│     biometric_result: String?       │
│     final_risk_level: String        │
│     test_duration_seconds: Number   │
│     available_tests: List<String>   │
│     device_type: String?            │
│     wearable_device_id: String?     │
│     created_at: String (ISO)        │
└─────────────────────────────────────┘
```

---

## 2. Enum 정의

### 2.1 DrinkType
```kotlin
enum class DrinkType {
    BEER,        // 맥주 🍺
    SOJU,        // 소주 🍶
    WINE,        // 와인 🍷
    WHISKY,      // 위스키 🥃
    HIGHBALL,    // 하이볼 🍹
    COCKTAIL,    // 칵테일 🍸
    MAKGEOLLI,   // 막걸리 🥛
    OTHER        // 기타 ⭐
}
```

### 2.2 Sex
```kotlin
enum class Sex {
    MALE,
    FEMALE,
    UNSET
}
```

### 2.3 DrinkingStatus
```kotlin
enum class DrinkingStatus {
    APPROPRIATE,  // 적정
    CAUTION,      // 주의
    EXCESSIVE,    // 과음
    DANGEROUS     // 위험
}
```

### 2.4 IntoxicationTestType
```kotlin
enum class IntoxicationTestType {
    FACE_ANALYSIS,    // 얼굴 분석
    HEART_RATE,       // 심박수 측정
    GYROSCOPE_TEST    // 자이로스코프 행동 검사
}
```

### 2.5 IntoxicationRiskLevel
```kotlin
enum class IntoxicationRiskLevel {
    HIGH,        // 상 (고위험)
    MEDIUM_HIGH, // 중상 (중간-고위험)
    MEDIUM,      // 중 (중간)
    MEDIUM_LOW,  // 중하 (중간-저위험)
    LOW          // 하 (저위험)
}
```

### 2.6 FaceAnalysisResult
```kotlin
enum class FaceAnalysisResult {
    NORMAL,   // 정상
    CAUTION,  // 주의
    DANGER    // 위험
}
```

### 2.7 BiometricResult
```kotlin
enum class BiometricResult {
    NORMAL,   // 정상
    CAUTION,  // 주의
    DANGER    // 위험
}
```

---

## 3. 관계도 (Relationships)

```
┌─────────────┐    1:1    ┌─────────────────┐
│    Users    │ ────────→ │  UserProfiles   │
│(회원가입/로그인)│         │ (개인 프로필 설정) │
└─────────────┘           └─────────────────┘
       │                           │
       │ 1:N                       │ 1:N
       ↓                           ↓
┌─────────────┐           ┌─────────────────┐
│DrinkRecords │           │  DailySummary   │
│(개별 음주기록)│           │ (캘린더 통계)     │
└─────────────┘           └─────────────────┘
       │                           ↑
       │ 1:N                       │ 집계
       ↓                           │
┌─────────────────────────────────────────────┐
│           IntoxicationTests                 │
│        (취함 정도 측정 데이터)                 │
└─────────────────────────────────────────────┘
```

---

## 4. 인덱스 및 쿼리 패턴

### 4.1 DynamoDB 쿼리 패턴
```
# 사용자별 모든 기록 조회
PK = user_id

# 특정 기간 기록 조회
PK = user_id AND SK BETWEEN record_id#2024-01 AND record_id#2024-02

# 날짜별 기록 조회 (GSI)
GSI: date-index
PK = user_id, SK = date
```

### 4.2 Global Secondary Index (GSI)
```
# 날짜별 조회용 GSI
GSI: date-user-index
PK = date (YYYY-MM-DD)
SK = user_id

# 사용자별 최신 기록 조회용 GSI
GSI: user-created-index
PK = user_id
SK = created_at
```

---

## 5. 데이터 플로우

### 5.1 음주 기록 생성
```
1. 사용자 입력/얼굴 분석 결과
2. DrinkRecords 테이블에 저장
3. DailySummary 재계산 및 업데이트
4. 캘린더 UI 실시간 반영
```

### 5.4 취함 정도 측정 (2단계 평가)
```
1단계: 얼굴 분석 (필수)
1. 사진 촬영 → AWS Rekognition 얼굴 분석
2. face_analysis_result = NORMAL/CAUTION/DANGER (3단계)

2단계: 생체 신호 분석 (선택)
1. 심박수 측정 (15% 가중치) → heart_rate 데이터
2. 자이로스코프 행동검사 (85% 가중치) → gyroscope 데이터
3. biometric_result = NORMAL/CAUTION/DANGER (3단계)

3단계: 최종 위험도 계산
1. face_analysis_result와 biometric_result 평균
2. final_risk_level = HIGH/MEDIUM_HIGH/MEDIUM/MEDIUM_LOW/LOW (5단계)

4. IntoxicationTests 테이블에 저장
5. DailySummary의 취함 정도 통계 업데이트
```

### 5.2 캘린더 표시
```
1. DailySummary 테이블 조회
2. UserProfiles에서 성별/연령 정보 조회
3. 개인별 기준으로 상태 색상 매핑 (적정/주의/과음/위험)
4. 선택일 DrinkRecords 상세 조회
```

### 5.3 통계 계산
```
1. DrinkRecords 기간별 쿼리
2. 표준잔수 계산
3. 트렌드 분석
4. 차트 데이터 생성
```

---

## 6. 표준잔수 계산 공식

```kotlin
// 알코올 그램 계산
fun calculateEthanolGrams(volumeMl: Int, abv: Float): Float {
    return volumeMl * (abv / 100) * 0.789f
}

// 표준잔수 계산
fun calculateStandardDrinks(ethanolGrams: Float): Float {
    return ethanolGrams / 14f
}

// 일일 상태 판정 (성별/연령별 기준 적용)
fun getDrinkingStatus(
    totalStdDrinks: Float, 
    sex: Sex, 
    isSenior65: Boolean
): DrinkingStatus {
    val thresholds = when {
        sex == Sex.FEMALE || isSenior65 -> {
            // 여성 또는 65세 이상 (g → 표준잔 변환)
            Triple(14f/14f, 42f/14f, 56f/14f) // 1.0, 3.0, 4.0 잔
        }
        else -> {
            // 성인 남성 (65세 미만) (g → 표준잔 변환)
            Triple(28f/14f, 56f/14f, 70f/14f) // 2.0, 4.0, 5.0 잔
        }
    }
    
    // 위험 기준 추가 처리
    val dangerousThreshold = when {
        sex == Sex.FEMALE || isSenior65 -> 80f/14f // 5.7 잔
        else -> 100f/14f // 7.1 잔
    }
    
    return when {
        totalStdDrinks <= thresholds.first -> DrinkingStatus.APPROPRIATE
        totalStdDrinks <= thresholds.second -> DrinkingStatus.CAUTION
        totalStdDrinks <= thresholds.third -> DrinkingStatus.EXCESSIVE
        totalStdDrinks <= dangerousThreshold -> DrinkingStatus.DANGEROUS
        else -> DrinkingStatus.DANGEROUS
    }
}
```

---

## 7. 데이터 보안 및 개인정보

### 7.1 암호화
- **전송**: HTTPS/TLS 1.3
- **저장**: DynamoDB 암호화
- **인증**: JWT 토큰 기반

### 7.2 개인정보 처리
- **최소 수집**: 서비스 필수 데이터만
- **익명화**: 통계 시 개인 식별 정보 제거
- **삭제권**: 사용자 요청 시 완전 삭제

---

## 8. 캘린더 화면 데이터 활용

### 8.1 월별 캘린더 그리드
- **DailySummary** 테이블에서 `status` 필드 조회
- **UserProfiles**에서 성별/연령 정보로 개인별 기준 적용
- 적정(APPROPRIATE) → 중립/녹색 계열
- 주의(CAUTION) → 연주황 색상
- 과음(EXCESSIVE) → 연적색 색상
- 위험(DANGEROUS) → 진적색 색상

**개인별 기준 (알코올 g → 표준잔):**
- 성인 남성(65세 미만): 28g(2.0잔) / 56g(4.0잔) / 70g(5.0잔) / 100g(7.1잔)
- 여성 또는 65세 이상: 14g(1.0잔) / 42g(3.0잔) / 56g(4.0잔) / 80g(5.7잔)

### 8.2 선택일 상세 기록
- **DrinkRecords** 테이블에서 해당 날짜 기록들 조회
- 주종별, 시간별 상세 정보 표시

### 8.3 통계 탭
- **DailySummary** 기간별 집계로 차트 생성
- **DrinkRecords**에서 주종별 통계 계산

---

---

## 9. 취함 정도 측정 시스템

### 9.1 측정 방법별 데이터

**📸 얼굴 분석 (Face Analysis)**
- AWS Rekognition 얼굴 검출로 눈 깜빡임, 얼굴 대칭성, 표정 변화 분석
- `face_analysis_result`: NORMAL/CAUTION/DANGER (3단계 결과)

**💓 심박수 측정 (Heart Rate)**
- 웨어러블 기기 연동으로 실시간 심박수 및 심박변이도 측정
- `heart_rate`: BPM 수치
- `heart_rate_variability`: 심박변이도 (ms)

**📱 자이로스코프 행동 검사 (Gyroscope Test)**
- 핸드폰을 이용한 균형감각 및 반응속도 테스트
- `gyroscope_stability`: 안정성 점수 (0-100)
- `gyroscope_reaction_time`: 반응시간 (ms)

### 9.2 취함 정도 계산 (2단계 평가)
```kotlin
// 1단계: 얼굴 분석 결과
fun analyzeFace(faceData: FaceData): FaceAnalysisResult {
    // AWS Rekognition 얼굴 분석 로직
    return when {
        faceData.isHighRisk() -> FaceAnalysisResult.DANGER
        faceData.isMediumRisk() -> FaceAnalysisResult.CAUTION
        else -> FaceAnalysisResult.NORMAL
    }
}

// 2단계: 생체신호 분석 결과 (선택)
fun analyzeBiometric(
    heartRate: Float?,
    heartRateVar: Float?,
    gyroStability: Float?,
    gyroReactionTime: Float?
): BiometricResult? {
    if (heartRate == null && gyroStability == null) return null
    
    var biometricScore = 0f
    var totalWeight = 0f
    
    // 심박수 (15% 가중치)
    heartRate?.let { hr ->
        val heartScore = normalizeHeartRate(hr, heartRateVar)
        biometricScore += heartScore * 0.15f
        totalWeight += 0.15f
    }
    
    // 자이로스코프 (85% 가중치)
    gyroStability?.let { gyro ->
        val gyroScore = normalizeGyroscope(gyro, gyroReactionTime)
        biometricScore += gyroScore * 0.85f
        totalWeight += 0.85f
    }
    
    val finalScore = biometricScore / totalWeight
    
    return when {
        finalScore >= 70f -> BiometricResult.DANGER
        finalScore >= 40f -> BiometricResult.CAUTION
        else -> BiometricResult.NORMAL
    }
}

// 3단계: 최종 위험도 계산
fun calculateFinalRiskLevel(
    faceResult: FaceAnalysisResult,
    biometricResult: BiometricResult?
): IntoxicationRiskLevel {
    return if (biometricResult == null) {
        // 얼굴 분석만 있는 경우
        when (faceResult) {
            FaceAnalysisResult.DANGER -> IntoxicationRiskLevel.MEDIUM_HIGH
            FaceAnalysisResult.CAUTION -> IntoxicationRiskLevel.MEDIUM
            FaceAnalysisResult.NORMAL -> IntoxicationRiskLevel.MEDIUM_LOW
        }
    } else {
        // 두 결과 평균
        val faceScore = when (faceResult) {
            FaceAnalysisResult.DANGER -> 3
            FaceAnalysisResult.CAUTION -> 2
            FaceAnalysisResult.NORMAL -> 1
        }
        
        val biometricScore = when (biometricResult) {
            BiometricResult.DANGER -> 3
            BiometricResult.CAUTION -> 2
            BiometricResult.NORMAL -> 1
        }
        
        val avgScore = (faceScore + biometricScore) / 2f
        
        when {
            avgScore >= 2.5f -> IntoxicationRiskLevel.HIGH
            avgScore >= 2.0f -> IntoxicationRiskLevel.MEDIUM_HIGH
            avgScore >= 1.5f -> IntoxicationRiskLevel.MEDIUM
            avgScore >= 1.0f -> IntoxicationRiskLevel.MEDIUM_LOW
            else -> IntoxicationRiskLevel.LOW
        }
    }
}
```

### 9.3 캘린더 연동
- **DailySummary**에 일별 평균/최대 취함 정도 저장
- 캘린더에서 음주량과 취함 정도를 함께 표시
- 취함 정도가 높은 날은 별도 아이콘으로 표시

---

이 ERD는 AlcoLook의 캘린더 음주기록 관리와 다중 센서 기반 취함 정도 측정을 위한 완전한 DynamoDB 기반 데이터 구조를 정의합니다. 클라우드 네이티브 구조로 확장성과 안정성을 보장합니다.