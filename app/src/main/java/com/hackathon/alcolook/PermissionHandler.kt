package com.hackathon.alcolook

import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.health.connect.client.HealthConnectClient
import androidx.health.connect.client.PermissionController
import androidx.health.connect.client.permission.HealthPermission
import androidx.health.connect.client.records.HeartRateRecord
import androidx.health.connect.client.records.ExerciseSessionRecord
import androidx.health.connect.client.request.ReadRecordsRequest
import androidx.health.connect.client.time.TimeRangeFilter
import java.time.Instant
import java.time.temporal.ChronoUnit

/**
 * Health Connect 권한 관리를 위한 통합 클래스
 */
class PermissionHandler(private val context: Context) {
    
    private val healthConnectClient by lazy { HealthConnectClient.getOrCreate(context) }
    
    val healthPermissions = setOf(
        HealthPermission.getReadPermission(HeartRateRecord::class),
        HealthPermission.getReadPermission(ExerciseSessionRecord::class)
        // READ_HEALTH_DATA_HISTORY는 자동으로 포함됨 (HeartRateRecord 읽기 시 필요)
    )
    
    suspend fun hasHealthPermissions(): Boolean {
        return try {
            healthConnectClient.permissionController.getGrantedPermissions()
                .containsAll(healthPermissions)
        } catch (e: Exception) {
            Log.e("PermissionHandler", "권한 확인 실패", e)
            false
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
    
    fun createHealthConnectSettingsIntent(): Intent? {
        return try {
            Intent().apply {
                action = "androidx.health.ACTION_MANAGE_HEALTH_PERMISSIONS"
                putExtra("androidx.health.EXTRA_PACKAGE_NAME", context.packageName)
            }
        } catch (e: Exception) {
            Log.e("PermissionHandler", "Health Connect 설정 Intent 생성 실패", e)
            null
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
}

@Composable
fun PermissionWaitingScreen(
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "AlcoLook",
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 32.dp)
        )
        
        CircularProgressIndicator(
            modifier = Modifier.padding(bottom = 16.dp)
        )
        
        Text(
            text = "Health Connect 권한을 확인하고 있습니다...",
            fontSize = 16.sp,
            modifier = Modifier.padding(bottom = 16.dp)
        )
        
        Text(
            text = "권한 요청 다이얼로그가 나타나면 '허용'을 선택해주세요.",
            fontSize = 14.sp,
            color = Color.Gray
        )
    }
}

@Composable
private fun PermissionItem(
    title: String,
    description: String
) {
    Column(
        modifier = Modifier.padding(vertical = 4.dp)
    ) {
        Text(
            text = "• $title",
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium
        )
        Text(
            text = description,
            fontSize = 12.sp,
            color = Color.Gray,
            modifier = Modifier.padding(start = 12.dp, top = 2.dp)
        )
    }
}
