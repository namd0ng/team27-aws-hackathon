package com.hackathon.alcolook

import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.health.connect.client.HealthConnectClient
import androidx.health.connect.client.permission.HealthPermission
import androidx.health.connect.client.records.HeartRateRecord
import androidx.health.connect.client.records.ExerciseSessionRecord
import androidx.health.connect.client.records.RestingHeartRateRecord
import androidx.health.connect.client.request.ReadRecordsRequest
import androidx.health.connect.client.time.TimeRangeFilter
import java.time.Instant
import java.time.temporal.ChronoUnit

class PermissionHandler(private val context: Context) {
    
    private val healthConnectClient = HealthConnectClient.getOrCreate(context)
    
    val healthPermissions = setOf(
        HealthPermission.getReadPermission(HeartRateRecord::class),
        HealthPermission.getReadPermission(ExerciseSessionRecord::class)
    ).let { basePermissions ->
        // Android 14 이상에서만 RestingHeartRateRecord 권한 추가
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            basePermissions + HealthPermission.getReadPermission(RestingHeartRateRecord::class)
        } else {
            basePermissions
        }
    }
    
    fun isHealthConnectAvailable(): Boolean {
        return try {
            val status = HealthConnectClient.getSdkStatus(context, "com.google.android.apps.healthdata")
            Log.d("PermissionHandler", "Health Connect SDK 상태: $status")
            status == HealthConnectClient.SDK_AVAILABLE
        } catch (e: Exception) {
            Log.e("PermissionHandler", "Health Connect 가용성 확인 실패", e)
            false
        }
    }
    
    suspend fun hasHealthPermissions(): Boolean {
        return try {
            val grantedPermissions = healthConnectClient.permissionController.getGrantedPermissions()
            healthPermissions.all { it in grantedPermissions }
        } catch (e: Exception) {
            Log.e("PermissionHandler", "권한 확인 실패", e)
            false
        }
    }
    
    suspend fun readRecentHeartRate(): HeartRateData? {
        return try {
            val now = Instant.now()
            val startTime = now.minus(1, ChronoUnit.HOURS)
            
            Log.d("PermissionHandler", "심박수 데이터 읽기 시작: $startTime ~ $now")
            
            val response = healthConnectClient.readRecords(
                ReadRecordsRequest(
                    HeartRateRecord::class,
                    timeRangeFilter = TimeRangeFilter.between(startTime, now)
                )
            )
            
            Log.d("PermissionHandler", "심박수 레코드 개수: ${response.records.size}")
            
            if (response.records.isNotEmpty()) {
                val latestRecord = response.records.maxByOrNull { it.startTime }
                latestRecord?.let { record ->
                    val sample = record.samples.lastOrNull()
                    if (sample != null) {
                        Log.d("PermissionHandler", "최신 심박수: ${sample.beatsPerMinute} BPM")
                        HeartRateData(
                            bpm = sample.beatsPerMinute,
                            timestamp = sample.time
                        )
                    } else {
                        Log.d("PermissionHandler", "심박수 샘플 없음")
                        null
                    }
                }
            } else {
                Log.d("PermissionHandler", "심박수 데이터 없음")
                null
            }
        } catch (e: Exception) {
            Log.e("PermissionHandler", "심박수 데이터 읽기 실패", e)
            null
        }
    }

    suspend fun getRestingHeartRateAverage(): Double? {
        // Android 14 이상에서 삼성 헬스 안정 시 심박수 시도
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            try {
                val now = Instant.now()
                val startTime = now.minus(7, ChronoUnit.DAYS)
                
                Log.d("PermissionHandler", "삼성 헬스 안정 시 심박수 조회 시도")
                
                val response = healthConnectClient.readRecords(
                    ReadRecordsRequest(
                        RestingHeartRateRecord::class,
                        timeRangeFilter = TimeRangeFilter.between(startTime, now)
                    )
                )
                
                if (response.records.isNotEmpty()) {
                    val latestRecord = response.records.maxByOrNull { it.time }
                    latestRecord?.let { record ->
                        val restingBpm = record.beatsPerMinute.toDouble()
                        Log.d("PermissionHandler", "삼성 헬스 안정 시 심박수: $restingBpm BPM")
                        return restingBpm
                    }
                }
            } catch (e: Exception) {
                Log.e("PermissionHandler", "삼성 헬스 안정 시 심박수 조회 실패", e)
            }
        }
        
        // Fallback: 직접 계산
        return try {
            val now = Instant.now()
            val startTime = now.minus(30, ChronoUnit.DAYS)
            
            Log.d("PermissionHandler", "안정 시 심박수 직접 계산")
            
            val response = healthConnectClient.readRecords(
                ReadRecordsRequest(
                    HeartRateRecord::class,
                    timeRangeFilter = TimeRangeFilter.between(startTime, now)
                )
            )
            
            if (response.records.isNotEmpty()) {
                val allSamples = response.records.flatMap { it.samples }
                
                if (allSamples.isNotEmpty()) {
                    val restingSamples = allSamples.filter { sample ->
                        sample.beatsPerMinute in 50..90
                    }
                    
                    if (restingSamples.isNotEmpty()) {
                        val average = restingSamples.map { it.beatsPerMinute.toDouble() }.average()
                        Log.d("PermissionHandler", "계산된 안정 시 심박수 평균: $average BPM")
                        average
                    } else null
                } else null
            } else null
        } catch (e: Exception) {
            Log.e("PermissionHandler", "안정 시 심박수 계산 실패", e)
            null
        }
    }
    
    fun createHealthConnectSettingsIntent(): Intent? {
        return try {
            Intent("androidx.health.ACTION_HEALTH_CONNECT_SETTINGS")
        } catch (e: Exception) {
            Log.e("PermissionHandler", "Health Connect 설정 Intent 생성 실패", e)
            null
        }
    }
}

fun analyzeHeartRateStatus(currentBpm: Long, restingAverage: Double?): HeartRateAnalysis {
    return if (restingAverage != null) {
        val difference = currentBpm - restingAverage
        val percentageIncrease = (difference / restingAverage) * 100
        
        Log.d("PermissionHandler", "심박수 분석: 현재=${currentBpm}, 평균=${restingAverage}, 차이=${difference}, 증가율=${percentageIncrease}%")
        
        when {
            // 50% 이상 증가: 매우 위험 (부정맥 위험)
            percentageIncrease >= 50 -> HeartRateAnalysis(
                status = HeartRateStatus.DANGEROUS,
                message = "위험 - 부정맥 위험",
                alcoholLevel = AlcoholLevel.HIGH,
                recommendation = "즉시 음주를 중단하고 휴식을 취하세요"
            )
            // 30-49% 증가: 과도한 음주 상태 (소주 2병 이상)
            percentageIncrease >= 30 -> HeartRateAnalysis(
                status = HeartRateStatus.ELEVATED,
                message = "상승 - 과도한 음주",
                alcoholLevel = AlcoholLevel.MODERATE,
                recommendation = "추가 음주를 피하고 수분을 섭취하세요"
            )
            // 20-29% 증가: 중간 음주 상태 (소주 1-2병)
            percentageIncrease >= 20 -> HeartRateAnalysis(
                status = HeartRateStatus.SLIGHTLY_ELEVATED,
                message = "약간 상승 - 주의 필요",
                alcoholLevel = AlcoholLevel.LOW,
                recommendation = "음주량을 조절하고 상태를 관찰하세요"
            )
            // 10-19% 증가: 정상적인 음주 반응
            percentageIncrease >= 10 -> HeartRateAnalysis(
                status = HeartRateStatus.NORMAL_ELEVATED,
                message = "정상적인 음주 반응",
                alcoholLevel = AlcoholLevel.MINIMAL,
                recommendation = "현재 상태를 유지하세요"
            )
            // 10% 미만 증가: 정상 범위
            else -> HeartRateAnalysis(
                status = HeartRateStatus.NORMAL,
                message = "정상 범위",
                alcoholLevel = AlcoholLevel.NONE,
                recommendation = "양호한 상태입니다"
            )
        }
    } else {
        // 기준 데이터가 없을 때: 일반적인 안정 시 심박수 70 BPM 기준으로 추정
        val estimatedResting = 70.0
        val difference = currentBpm - estimatedResting
        val percentageIncrease = (difference / estimatedResting) * 100
        
        Log.d("PermissionHandler", "추정 기준 분석: 현재=${currentBpm}, 추정평균=${estimatedResting}, 증가율=${percentageIncrease}%")
        
        when {
            percentageIncrease >= 50 -> HeartRateAnalysis(
                status = HeartRateStatus.DANGEROUS,
                message = "위험 - 부정맥 위험 (추정)",
                alcoholLevel = AlcoholLevel.HIGH,
                recommendation = "즉시 음주를 중단하고 휴식을 취하세요"
            )
            percentageIncrease >= 30 -> HeartRateAnalysis(
                status = HeartRateStatus.ELEVATED,
                message = "상승 - 음주 의심 (추정)",
                alcoholLevel = AlcoholLevel.MODERATE,
                recommendation = "추가 음주를 피하고 수분을 섭취하세요"
            )
            percentageIncrease >= 20 -> HeartRateAnalysis(
                status = HeartRateStatus.SLIGHTLY_ELEVATED,
                message = "약간 상승 (추정)",
                alcoholLevel = AlcoholLevel.LOW,
                recommendation = "상태를 관찰하세요"
            )
            percentageIncrease >= 10 -> HeartRateAnalysis(
                status = HeartRateStatus.NORMAL_ELEVATED,
                message = "정상적인 반응 (추정)",
                alcoholLevel = AlcoholLevel.MINIMAL,
                recommendation = "현재 상태를 유지하세요"
            )
            else -> HeartRateAnalysis(
                status = HeartRateStatus.NORMAL,
                message = "정상 범위 (추정)",
                alcoholLevel = AlcoholLevel.NONE,
                recommendation = "양호한 상태입니다"
            )
        }
    }
}

data class HeartRateData(
    val bpm: Long,
    val timestamp: Instant
)

data class HeartRateAnalysis(
    val status: HeartRateStatus,
    val message: String,
    val alcoholLevel: AlcoholLevel,
    val recommendation: String
)

enum class HeartRateStatus {
    NORMAL,              // 정상
    NORMAL_ELEVATED,     // 정상적인 음주 반응
    SLIGHTLY_ELEVATED,   // 약간 상승
    ELEVATED,           // 상승 (음주 상태)
    DANGEROUS           // 위험 (부정맥 위험)
}

enum class AlcoholLevel {
    NONE,      // 음주 없음
    MINIMAL,   // 최소 음주
    LOW,       // 경미한 음주
    MODERATE,  // 중간 음주 (소주 1-2병)
    HIGH       // 과도한 음주
}
