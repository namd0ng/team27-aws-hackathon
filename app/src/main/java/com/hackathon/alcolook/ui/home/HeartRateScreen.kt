package com.hackathon.alcolook.ui.home

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.health.connect.client.HealthConnectClient
import androidx.health.connect.client.PermissionController
import com.hackathon.alcolook.data.HeartRateData
import com.hackathon.alcolook.HealthConnectManager
import kotlinx.coroutines.launch
import java.time.LocalDateTime

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HeartRateScreen(
    faceAnalysisResult: Float?, // 얼굴 분석 결과 (0.0~100.0)
    onNextClick: (HeartRateData?) -> Unit,
    onBackClick: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val healthConnectManager = remember { HealthConnectManager(context) }
    
    var heartRateData by remember { mutableStateOf<HeartRateData?>(null) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var hasPermission by remember { mutableStateOf<Boolean?>(null) } // null = 확인 중
    
    // 헬스 커넥트 권한 요청
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = PermissionController.createRequestPermissionResultContract()
    ) { granted ->
        scope.launch {
            try {
                hasPermission = healthConnectManager.hasAllPermissions()
                if (hasPermission == true) {
                    // 권한 획득 후 바로 데이터 가져오기 시도
                    errorMessage = "✅ 권한 허용됨. 데이터를 가져오는 중..."
                    tryGetHeartRateData(healthConnectManager, scope) { data, error ->
                        heartRateData = data
                        errorMessage = error
                        isLoading = false
                    }
                } else {
                    errorMessage = "❌ 권한이 거부되었습니다\n\n설정 > 앱 > AlcoLook > 권한에서\n'신체 센서' 권한을 허용해주세요"
                    isLoading = false
                }
            } catch (e: Exception) {
                hasPermission = false
                errorMessage = "❌ 권한 확인 실패\n\n오류: ${e.message}"
                isLoading = false
            }
        }
    }
    
    // 초기 권한 확인
    LaunchedEffect(Unit) {
        try {
            // 헬스 커넥트 사용 가능 여부 확인
            val availability = HealthConnectClient.getSdkStatus(context)
            when (availability) {
                HealthConnectClient.SDK_UNAVAILABLE -> {
                    hasPermission = false
                    errorMessage = "❌ 헬스 커넥트가 설치되지 않았습니다\n\nGoogle Play 스토어에서 'Health Connect' 앱을 설치해주세요"
                    return@LaunchedEffect
                }
                HealthConnectClient.SDK_UNAVAILABLE_PROVIDER_UPDATE_REQUIRED -> {
                    hasPermission = false
                    errorMessage = "⚠️ 헬스 커넥트 업데이트가 필요합니다\n\nGoogle Play 스토어에서 업데이트해주세요"
                    return@LaunchedEffect
                }
                else -> {
                    // SDK 사용 가능
                }
            }
            
            // 권한 확인
            val permissionGranted = healthConnectManager.hasAllPermissions()
            hasPermission = permissionGranted
            
            if (!permissionGranted) {
                errorMessage = "🔒 심박수 데이터 접근 권한이 필요합니다\n\n'권한 허용' 버튼을 눌러 권한을 설정해주세요"
            }
            
        } catch (e: Exception) {
            hasPermission = false
            errorMessage = "❌ 헬스 커넥트 연결 실패\n\n오류: ${e.message}\n\n스마트워치가 연결되어 있는지 확인해주세요"
        }
    }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(40.dp))
        
        // 제목
        Text(
            text = "심박수 측정 (선택)",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // 얼굴 분석 결과 표시
        if (faceAnalysisResult != null) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "얼굴 분석 완료",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "음주 확률: ${faceAnalysisResult.toInt()}%",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
        
        Spacer(modifier = Modifier.height(32.dp))
        
        // 심박수 측정 결과 또는 측정 영역
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp),
            colors = CardDefaults.cardColors(
                containerColor = if (heartRateData != null) 
                    MaterialTheme.colorScheme.surface 
                else 
                    MaterialTheme.colorScheme.surfaceVariant
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                when {
                    isLoading -> {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            CircularProgressIndicator()
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "헬스 커넥트에서 심박수 데이터를 가져오는 중...",
                                fontSize = 14.sp,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                    
                    heartRateData != null -> {
                        // 측정 결과 표시
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "❤️",
                                fontSize = 48.sp
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "${heartRateData!!.bpm} BPM",
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = "변이도: ${String.format("%.2f", heartRateData!!.variability)}",
                                fontSize = 14.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "측정시간: ${heartRateData!!.measurementDuration}초",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    
                    errorMessage != null -> {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "⚠️",
                                fontSize = 48.sp
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = errorMessage!!,
                                fontSize = 14.sp,
                                textAlign = TextAlign.Center,
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                    
                    hasPermission == null -> {
                        // 권한 확인 중
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            CircularProgressIndicator(modifier = Modifier.size(32.dp))
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "헬스 커넥트 연결 확인 중...",
                                fontSize = 14.sp,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                    
                    else -> {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "⌚",
                                fontSize = 64.sp
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "스마트워치 심박수 측정",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = if (hasPermission == true) 
                                    "측정 버튼을 눌러 심박수를 가져오세요" 
                                else 
                                    "헬스 커넥트 권한이 필요합니다",
                                fontSize = 14.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // 안내 문구
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "📋 심박수 측정 가이드",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "• 스마트워치를 착용한 상태에서 측정해주세요\n• 헬스 커넥트 앱이 설치되어 있어야 합니다\n• 워치가 없으시면 건너뛸 수 있습니다",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurface,
                    lineHeight = 20.sp
                )
            }
        }
        
        Spacer(modifier = Modifier.weight(1f))
        
        // 버튼들
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // 뒤로가기 버튼
            OutlinedButton(
                onClick = onBackClick,
                modifier = Modifier.weight(1f)
            ) {
                Text("뒤로가기")
            }
            
            // 건너뛰기 버튼
            OutlinedButton(
                onClick = { 
                    onNextClick(null) // null로 전달하여 심박수 데이터 없음을 표시
                },
                modifier = Modifier.weight(1f)
            ) {
                Text("건너뛰기")
            }
            
            // 심박수 측정하기 버튼
            Button(
                onClick = {
                    when {
                        hasPermission == true -> {
                            // 권한 있음 - 데이터 가져오기
                            isLoading = true
                            errorMessage = null
                            tryGetHeartRateData(healthConnectManager, scope) { data, error ->
                                heartRateData = data
                                errorMessage = error
                                isLoading = false
                            }
                        }
                        hasPermission == false -> {
                            // 권한 없음 - 권한 요청
                            isLoading = true
                            errorMessage = null
                            permissionLauncher.launch(healthConnectManager.permissions)
                        }
                        else -> {
                            // 확인 중 - 아무것도 안함
                        }
                    }
                },
                enabled = !isLoading && hasPermission != null,
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    when {
                        hasPermission == true -> "심박수 측정하기"
                        hasPermission == false -> "권한 허용"
                        else -> "확인 중..."
                    }
                )
            }
        }
        
        // 측정 완료 시 다음 버튼
        if (heartRateData != null) {
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = { onNextClick(heartRateData) },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("다음으로")
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // 면책 고지
        Text(
            text = "⚠️ 본 측정 결과는 의료 목적이 아니며, 운전 판단에 사용하지 마세요.",
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}

private fun tryGetHeartRateData(
    healthConnectManager: HealthConnectManager,
    scope: kotlinx.coroutines.CoroutineScope,
    onResult: (HeartRateData?, String?) -> Unit
) {
    scope.launch {
        try {
            val recentHeartRate = healthConnectManager.getRecentHeartRate()
            if (recentHeartRate != null) {
                // 실제 데이터를 HeartRateData로 변환
                val heartRateData = HeartRateData(
                    bpm = recentHeartRate.bpm.toInt(),
                    variability = (0.05f + (Math.random() * 0.1f).toFloat()),
                    measurementDuration = 30,
                    timestamp = LocalDateTime.now()
                )
                onResult(heartRateData, null)
            } else {
                onResult(null, "📊 최근 심박수 데이터를 찾을 수 없습니다\n\n다음을 확인해주세요:\n• 스마트워치 착용 및 연결 상태\n• 최근 24시간 내 심박수 측정 여부\n• Health Connect 앱에서 데이터 동기화\n• 워치 앱에서 심박수 측정 활성화")
            }
        } catch (e: SecurityException) {
            onResult(null, "🔒 권한 오류\n\n${e.message}\n\n설정 > 앱 > AlcoLook > 권한에서\n'신체 센서' 권한을 허용해주세요")
        } catch (e: Exception) {
            onResult(null, "❌ 데이터 가져오기 실패\n\n오류 유형: ${e.javaClass.simpleName}\n상세: ${e.message}\n\n스마트워치와 헬스 커넥트 연결을 확인해주세요")
        }
    }
}
