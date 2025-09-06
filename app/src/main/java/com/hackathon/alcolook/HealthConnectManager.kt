package com.hackathon.alcolook

import android.content.Context
import androidx.health.connect.client.HealthConnectClient
import androidx.health.connect.client.permission.HealthPermission
import androidx.health.connect.client.records.HeartRateRecord
import androidx.health.connect.client.request.ReadRecordsRequest
import androidx.health.connect.client.time.TimeRangeFilter
import java.time.Instant
import java.time.temporal.ChronoUnit

// 헬스 커넥트용 HeartRateData (임시)
data class HealthConnectHeartRateData(
    val bpm: Long,
    val timestamp: Instant
)

class HealthConnectManager(private val context: Context) {
    
    private val healthConnectClient by lazy { HealthConnectClient.getOrCreate(context) }
    
    val permissions = setOf(
        HealthPermission.getReadPermission(HeartRateRecord::class)
    )
    
    suspend fun hasAllPermissions(): Boolean {
        return try {
            val grantedPermissions = healthConnectClient.permissionController.getGrantedPermissions()
            grantedPermissions.contains(HealthPermission.getReadPermission(HeartRateRecord::class))
        } catch (e: Exception) {
            false
        }
    }
    
    suspend fun getRecentHeartRate(): HealthConnectHeartRateData? {
        return try {
            val now = Instant.now()
            val request = ReadRecordsRequest(
                recordType = HeartRateRecord::class,
                timeRangeFilter = TimeRangeFilter.between(
                    now.minus(24, ChronoUnit.HOURS),
                    now
                )
            )
            
            val response = healthConnectClient.readRecords(request)
            val records = response.records
            
            if (records.isNotEmpty()) {
                val latestRecord = records.maxByOrNull { it.startTime }
                latestRecord?.let { record ->
                    HealthConnectHeartRateData(
                        bpm = record.samples.lastOrNull()?.beatsPerMinute ?: 0L,
                        timestamp = record.startTime
                    )
                }
            } else {
                null // 데이터 없음
            }
        } catch (e: Exception) {
            throw e // 예외를 그대로 전달
        }
    }
    
    suspend fun getBaselineHeartRate(userAge: Int): Double? {
        return try {
            val now = Instant.now()
            val startTime = now.minus(7, ChronoUnit.DAYS)
            val endTime = now.minus(1, ChronoUnit.DAYS)
            
            val heartRateRequest = ReadRecordsRequest(
                recordType = HeartRateRecord::class,
                timeRangeFilter = TimeRangeFilter.between(startTime, endTime)
            )
            val heartRateResponse = healthConnectClient.readRecords(heartRateRequest)
            
            val normalRange = getNormalHeartRateRange(userAge)
            
            val restingHeartRates = heartRateResponse.records.flatMap { record ->
                record.samples.filter { sample ->
                    sample.beatsPerMinute in normalRange.first..normalRange.second
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
