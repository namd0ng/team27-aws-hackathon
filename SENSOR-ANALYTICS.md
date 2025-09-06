# AlcoLook Sensor Analytics Infrastructure

## 📊 개요

Android 앱의 센서 데이터(자이로스코프, 심박수)를 익명화하여 분석하는 서버리스 인프라입니다.

## 🏗️ 아키텍처

```
Android App → S3 Bucket → Lambda Functions → CloudWatch Analytics
     ↓              ↓              ↓              ↓
센서 데이터    익명화 저장    실시간 분석    대시보드 시각화
```

## 🔧 구성 요소

### 1. **S3 Bucket** (`sensor-analytics`)
- **목적**: 익명화된 센서 데이터 저장
- **보안**: 완전 비공개, 30일 자동 삭제
- **구조**:
  ```
  gyroscope/
  ├── 2025/01/06/gyro-data-123.json
  └── 2025/01/06/gyro-data-124.json
  heartrate/
  ├── 2025/01/06/hr-data-123.json
  └── 2025/01/06/hr-data-124.json
  ```

### 2. **Lambda Functions**
- **Gyroscope Analysis**: 균형감각 안정성 분석
- **Heart Rate Analysis**: 심박 변이도 분석
- **트리거**: S3 객체 생성 시 자동 실행

### 3. **CloudWatch Dashboard**
- **실시간 모니터링**: Lambda 실행 횟수, 지연시간, 오류
- **스토리지 현황**: S3 객체 수, 용량 사용량

## 🚀 배포 방법

### CloudFormation
```bash
./deploy-sensor-analytics.sh dev cloudformation
```

### Terraform
```bash
./deploy-sensor-analytics.sh dev terraform
```

### AWS CDK
```bash
./deploy-sensor-analytics.sh dev cdk
```

## 📱 Android 앱 연동

### 1. 자이로스코프 데이터 전송
```kotlin
// GyroscopeManager.kt에서 익명화된 데이터 전송
private fun uploadAnonymizedData(gyroData: GyroscopeData) {
    val anonymizedData = mapOf(
        "timestamp" to System.currentTimeMillis(),
        "movements" to gyroData.movements.map { 
            mapOf("magnitude" to it.magnitude) 
        },
        "stability_score" to gyroData.stabilityScore
        // 개인정보 제외
    )
    
    // S3에 업로드
    s3Client.putObject(
        bucket = "alcolook-sensor-analytics-dev",
        key = "gyroscope/${LocalDate.now()}/gyro-${UUID.randomUUID()}.json",
        body = Json.encodeToString(anonymizedData)
    )
}
```

### 2. 심박수 데이터 전송
```kotlin
// HealthConnectManager.kt에서 익명화된 데이터 전송
private fun uploadAnonymizedHeartRate(heartRateData: HeartRateData) {
    val anonymizedData = mapOf(
        "timestamp" to System.currentTimeMillis(),
        "bpm" to heartRateData.bpm,
        "resting_bpm" to 70, // 평균값으로 익명화
        "variability" to heartRateData.variability
        // 개인 식별 정보 제외
    )
    
    // S3에 업로드
    s3Client.putObject(
        bucket = "alcolook-sensor-analytics-dev",
        key = "heartrate/${LocalDate.now()}/hr-${UUID.randomUUID()}.json",
        body = Json.encodeToString(anonymizedData)
    )
}
```

## 📈 분석 결과

### 자이로스코프 분석
- **안정성 점수**: 0.0 (불안정) ~ 1.0 (안정)
- **계산 방식**: `1 - (평균 움직임 크기 / 5.0)`
- **활용**: 균형감각 상태 평가

### 심박수 분석
- **HRV 점수**: 심박 변이도 기반
- **계산 방식**: `|현재 BPM - 안정시 BPM| / 안정시 BPM`
- **활용**: 스트레스/음주 상태 추정

## 🔒 개인정보 보호

### ✅ 보호 조치
- **완전 익명화**: 개인 식별 정보 전송 금지
- **자동 삭제**: 30일 후 데이터 자동 삭제
- **최소 권한**: Lambda는 분석에 필요한 권한만 보유
- **암호화**: S3 저장 시 자동 암호화

### ❌ 수집하지 않는 데이터
- 사용자 ID, 이름, 연락처
- 정확한 위치 정보
- 기기 고유 식별자
- 개인 건강 기록

## 🛠️ 운영 및 모니터링

### CloudWatch 메트릭
- **Lambda 실행 횟수**: 일일 센서 데이터 처리량
- **오류율**: 분석 실패 비율
- **지연시간**: 평균 처리 시간
- **S3 사용량**: 저장된 데이터 크기

### 알람 설정
```bash
# 오류율 5% 초과 시 알람
aws cloudwatch put-metric-alarm \
  --alarm-name "AlcoLook-Lambda-Errors" \
  --alarm-description "Lambda error rate > 5%" \
  --metric-name Errors \
  --namespace AWS/Lambda \
  --statistic Sum \
  --period 300 \
  --threshold 5 \
  --comparison-operator GreaterThanThreshold
```

## 💰 비용 예상

### 월간 예상 비용 (1000명 사용자 기준)
- **S3 스토리지**: ~$1 (30일 보관)
- **Lambda 실행**: ~$2 (일일 분석)
- **CloudWatch**: ~$1 (로그 및 메트릭)
- **총 예상 비용**: ~$4/월

## 🔄 확장 계획

### Phase 1: 기본 분석 (현재)
- 자이로스코프 안정성 분석
- 심박수 변이도 분석

### Phase 2: 고급 분석
- 머신러닝 기반 패턴 인식
- 시계열 데이터 분석
- 예측 모델링

### Phase 3: 실시간 피드백
- API Gateway 추가
- 실시간 분석 결과 제공
- 개인화된 권장사항

## 📞 지원

문제 발생 시:
1. CloudWatch 로그 확인
2. Lambda 함수 상태 점검
3. S3 버킷 권한 확인
4. GitHub Issues에 문의
