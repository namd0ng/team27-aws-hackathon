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

data class HeartRateData(
    val bpm: Long,
    val timestamp: Instant
)

enum class HeartRateStatus {
    NORMAL, NORMAL_ELEVATED, SLIGHTLY_ELEVATED, ELEVATED, DANGEROUS
}

enum class AlcoholLevel {
    NONE, MINIMAL, LOW, MODERATE, HIGH
}

data class HeartRateAnalysis(
    val status: HeartRateStatus,
    val message: String,
    val alcoholLevel: AlcoholLevel,
    val recommendation: String
)

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
