package com.hackathon.alcolook

import android.content.Context
import androidx.health.connect.client.HealthConnectClient
import androidx.health.connect.client.permission.HealthPermission
import androidx.health.connect.client.records.HeartRateRecord
import androidx.health.connect.client.records.ExerciseSessionRecord
import androidx.health.connect.client.request.ReadRecordsRequest
import androidx.health.connect.client.time.TimeRangeFilter
import java.time.Instant
import java.time.temporal.ChronoUnit

class HealthConnectManager(private val context: Context) {
    
    private val healthConnectClient by lazy { HealthConnectClient.getOrCreate(context) }
    
    val permissions = setOf(
        HealthPermission.getReadPermission(HeartRateRecord::class),
        HealthPermission.getReadPermission(ExerciseSessionRecord::class)
    )
    
    suspend fun hasAllPermissions(): Boolean {
        return try {
            healthConnectClient.permissionController.getGrantedPermissions()
                .containsAll(permissions)
        } catch (e: Exception) {
            false
        }
    }
    
    suspend fun getRecentHeartRate(): HeartRateData? {
        return try {
            val now = Instant.now()
            val request = ReadRecordsRequest(
                recordType = HeartRateRecord::class,
                timeRangeFilter = TimeRangeFilter.between(
                    now.minus(1, ChronoUnit.HOURS),
                    now
                )
            )
            
            val response = healthConnectClient.readRecords(request)
            val records = response.records
            
            if (records.isNotEmpty()) {
                val latestRecord = records.maxByOrNull { it.startTime }
                latestRecord?.let { record ->
                    HeartRateData(
                        bpm = record.samples.lastOrNull()?.beatsPerMinute ?: 0L,
                        timestamp = record.startTime
                    )
                }
            } else null
        } catch (e: Exception) {
            null
        }
    }
    
    suspend fun getBaselineHeartRate(userAge: Int): Double? {
        return try {
            val now = Instant.now()
            val startTime = now.minus(7, ChronoUnit.DAYS)
            val endTime = now.minus(1, ChronoUnit.DAYS)
            
            // 운동 세션 데이터 가져오기
            val exerciseRequest = ReadRecordsRequest(
                recordType = ExerciseSessionRecord::class,
                timeRangeFilter = TimeRangeFilter.between(startTime, endTime)
            )
            val exerciseResponse = healthConnectClient.readRecords(exerciseRequest)
            val exerciseSessions = exerciseResponse.records
            
            // 심박수 데이터 가져오기
            val heartRateRequest = ReadRecordsRequest(
                recordType = HeartRateRecord::class,
                timeRangeFilter = TimeRangeFilter.between(startTime, endTime)
            )
            val heartRateResponse = healthConnectClient.readRecords(heartRateRequest)
            
            // 나이대별 정상 심박수 범위
            val normalRange = getNormalHeartRateRange(userAge)
            
            // 운동 중이 아닌 심박수만 필터링
            val restingHeartRates = heartRateResponse.records.flatMap { record ->
                record.samples.filter { sample ->
                    val sampleTime = sample.time
                    // 운동 세션과 겹치지 않는 시간대의 심박수만 선택
                    exerciseSessions.none { session ->
                        sampleTime.isAfter(session.startTime) && sampleTime.isBefore(session.endTime)
                    } && sample.beatsPerMinute in normalRange.first..normalRange.second
                }
            }
            
            if (restingHeartRates.isNotEmpty()) {
                restingHeartRates.map { it.beatsPerMinute.toDouble() }.average()
            } else null
        } catch (e: Exception) {
            null
        }
    }
    
    private fun getNormalHeartRateRange(age: Int): Pair<Int, Int> {
        return when (age) {
            in 18..25 -> Pair(60, 100)
            in 26..35 -> Pair(60, 95)
            in 36..45 -> Pair(60, 90)
            in 46..55 -> Pair(60, 85)
            in 56..65 -> Pair(60, 80)
            else -> Pair(60, 100) // 기본값
        }
    }
}

data class HeartRateData(
    val bpm: Long,
    val timestamp: Instant
)
