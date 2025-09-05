package com.hackathon.alcolook

import android.content.Context
import androidx.health.connect.client.HealthConnectClient
import androidx.health.connect.client.permission.HealthPermission
import androidx.health.connect.client.records.HeartRateRecord
import androidx.health.connect.client.request.ReadRecordsRequest
import androidx.health.connect.client.time.TimeRangeFilter
import java.time.Instant
import java.time.temporal.ChronoUnit

data class HeartRateData(
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
}
