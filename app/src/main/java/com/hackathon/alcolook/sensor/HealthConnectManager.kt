package com.hackathon.alcolook.sensor

import android.content.Context
import androidx.health.connect.client.HealthConnectClient
import androidx.health.connect.client.permission.HealthPermission
import androidx.health.connect.client.records.RestingHeartRateRecord
import androidx.health.connect.client.records.HeartRateRecord
import androidx.health.connect.client.request.ReadRecordsRequest
import androidx.health.connect.client.time.TimeRangeFilter
import com.hackathon.alcolook.data.HeartRateData
import java.time.Instant
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit

class HealthConnectManager(private val context: Context) {
    
    private val healthConnectClient by lazy { HealthConnectClient.getOrCreate(context) }
    
    val permissions = setOf(
        HealthPermission.getReadPermission(RestingHeartRateRecord::class),
        HealthPermission.getReadPermission(HeartRateRecord::class)
    )
    
    suspend fun hasAllPermissions(): Boolean {
        return try {
            println("🔍 권한 확인 중...")
            val grantedPermissions = healthConnectClient.permissionController.getGrantedPermissions()
            val hasRestingHR = grantedPermissions.contains(HealthPermission.getReadPermission(RestingHeartRateRecord::class))
            val hasHeartRate = grantedPermissions.contains(HealthPermission.getReadPermission(HeartRateRecord::class))
            
            println("📋 RestingHeartRateRecord 권한: ${if (hasRestingHR) "✅" else "❌"}")
            println("📋 HeartRateRecord 권한: ${if (hasHeartRate) "✅" else "❌"}")
            
            hasRestingHR && hasHeartRate
        } catch (e: Exception) {
            println("❌ 권한 확인 실패: ${e.message}")
            false
        }
    }
    
    suspend fun getRestingHeartRate(): HeartRateData? {
        return try {
            println("🔍 안정시 심박수 가져오기 시작")
            
            // 먼저 RestingHeartRateRecord 시도
            val restingData = getRestingHeartRateFromRecord()
            if (restingData != null) {
                println("✅ RestingHeartRateRecord에서 데이터 발견: ${restingData.bpm} BPM")
                return restingData
            }
            
            println("⚠️ RestingHeartRateRecord 없음, HeartRateRecord에서 추정 시도")
            // 없으면 일반 HeartRateRecord에서 안정시 추정
            val estimatedData = getRestingHeartRateFromHeartRate()
            if (estimatedData != null) {
                println("✅ HeartRateRecord에서 추정: ${estimatedData.bpm} BPM")
                return estimatedData
            }
            
            println("❌ 모든 데이터 소스에서 실패")
            null
        } catch (e: Exception) {
            println("❌ 안정시 심박수 가져오기 오류: ${e.message}")
            null
        }
    }
    
    private suspend fun getRestingHeartRateFromRecord(): HeartRateData? {
        val now = Instant.now()
        val request = ReadRecordsRequest(
            recordType = RestingHeartRateRecord::class,
            timeRangeFilter = TimeRangeFilter.between(
                now.minus(30, ChronoUnit.DAYS), // 30일로 확장
                now
            )
        )
        
        println("🔍 RestingHeartRateRecord 조회 중 (최근 30일)")
        val response = healthConnectClient.readRecords(request)
        println("📊 RestingHeartRateRecord 개수: ${response.records.size}")
        
        return response.records.maxByOrNull { it.time }?.let { record ->
            println("✅ 최신 RestingHeartRateRecord: ${record.beatsPerMinute} BPM (${record.time})")
            HeartRateData(
                bpm = record.beatsPerMinute.toInt(),
                variability = 0.04f,
                measurementDuration = 60,
                timestamp = LocalDateTime.now()
            )
        }
    }
    
    private suspend fun getRestingHeartRateFromHeartRate(): HeartRateData? {
        val now = Instant.now()
        val request = ReadRecordsRequest(
            recordType = HeartRateRecord::class,
            timeRangeFilter = TimeRangeFilter.between(
                now.minus(7, ChronoUnit.DAYS), // 7일간 데이터
                now
            )
        )
        
        println("🔍 HeartRateRecord 조회 중 (최근 7일)")
        val response = healthConnectClient.readRecords(request)
        val allSamples = response.records.flatMap { it.samples }
        println("📊 HeartRateRecord 샘플 개수: ${allSamples.size}")
        
        if (allSamples.isNotEmpty()) {
            // 가장 낮은 심박수들의 평균 (안정시 추정)
            val sortedBpm = allSamples.map { it.beatsPerMinute }.sorted()
            val bottomQuartile = sortedBpm.take(sortedBpm.size / 4)
            val avgRestingBpm = if (bottomQuartile.isNotEmpty()) {
                bottomQuartile.average().toInt()
            } else {
                sortedBpm.first().toInt()
            }
            
            println("✅ 추정된 안정시 심박수: $avgRestingBpm BPM (하위 25% 평균)")
            println("📈 전체 범위: ${sortedBpm.first()}-${sortedBpm.last()} BPM")
            
            return HeartRateData(
                bpm = avgRestingBpm,
                variability = 0.04f,
                measurementDuration = 60,
                timestamp = LocalDateTime.now()
            )
        }
        
        println("❌ HeartRateRecord 샘플 없음")
        return null
    }
    
    suspend fun getLatestHeartRate(): HeartRateData {
        return try {
            // 최근 HeartRateRecord에서 가져오기
            val now = Instant.now()
            val request = ReadRecordsRequest(
                recordType = HeartRateRecord::class,
                timeRangeFilter = TimeRangeFilter.between(
                    now.minus(1, ChronoUnit.HOURS),
                    now
                )
            )
            
            val response = healthConnectClient.readRecords(request)
            val latestRecord = response.records.maxByOrNull { it.startTime }
            
            latestRecord?.let { record ->
                val latestSample = record.samples.maxByOrNull { it.time }
                latestSample?.let { sample ->
                    HeartRateData(
                        bpm = sample.beatsPerMinute.toInt(),
                        variability = 0.05f,
                        measurementDuration = 30,
                        timestamp = LocalDateTime.now()
                    )
                }
            } ?: getSimulatedCurrentHeartRate()
        } catch (e: Exception) {
            getSimulatedCurrentHeartRate()
        }
    }
    
    private fun getSimulatedCurrentHeartRate(): HeartRateData {
        // 실제 데이터가 없을 때만 시뮬레이션
        val baseBpm = 70 // 기본 안정시
        val variation = (-10..20).random() // 음주/스트레스 시뮬레이션
        
        return HeartRateData(
            bpm = baseBpm + variation,
            variability = 0.05f + (kotlin.random.Random.nextFloat() * 0.03f),
            measurementDuration = 30,
            timestamp = LocalDateTime.now()
        )
    }
}
