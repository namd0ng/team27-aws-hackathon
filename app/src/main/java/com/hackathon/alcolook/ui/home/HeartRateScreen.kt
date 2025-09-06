package com.hackathon.alcolook.ui.home

import androidx.activity.compose.rememberLauncherForActivityResult
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
import androidx.health.connect.client.PermissionController
import com.hackathon.alcolook.data.HeartRateData
import com.hackathon.alcolook.PermissionHandler
import kotlinx.coroutines.launch
import java.time.LocalDateTime

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HeartRateScreen(
    faceAnalysisResult: Float?,
    onNextClick: (HeartRateData?) -> Unit,
    onBackClick: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val permissionHandler: PermissionHandler = remember { PermissionHandler(context) }
    
    var restingHeartRate: HeartRateData? by remember { mutableStateOf(null) }
    var currentHeartRate: HeartRateData? by remember { mutableStateOf(null) }
    var isLoading by remember { mutableStateOf(false) }
    var hasPermissions: Boolean? by remember { mutableStateOf(null) }
    var errorMessage: String? by remember { mutableStateOf(null) }
    
    // 권한 요청 런처
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = PermissionController.createRequestPermissionResultContract()
    ) { granted ->
        scope.launch {
            hasPermissions = permissionHandler.hasHealthPermissions()
            if (hasPermissions == true) {
                loadRestingHeartRate(permissionHandler) { data ->
                    restingHeartRate = data
                }
            }
        }
    }
    
    // 초기 권한 확인 및 데이터 로드
    LaunchedEffect(Unit) {
        try {
            println("🚀 HeartRateScreen: 초기화 시작")
            
            if (!permissionHandler.isHealthConnectAvailable()) {
                errorMessage = "Health Connect가 설치되지 않았습니다"
                return@LaunchedEffect
            }
            
            hasPermissions = permissionHandler.hasHealthPermissions()
            println("🔐 권한 상태: ${hasPermissions}")
            
            if (hasPermissions == true) {
                loadRestingHeartRate(permissionHandler) { data ->
                    restingHeartRate = data
                }
            }
        } catch (e: Exception) {
            println("❌ 초기화 실패: ${e.message}")
            errorMessage = "초기화 실패: ${e.message}"
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(20.dp))
        
        // 제목
        Text(
            text = "심박수 측정 (선택)",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // 측정 진행 상황
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
                    text = "측정 진행 상황",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(8.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("👤", fontSize = 20.sp)
                        Text(
                            text = if (faceAnalysisResult != null) "완료" else "대기",
                            fontSize = 12.sp,
                            color = if (faceAnalysisResult != null) 
                                MaterialTheme.colorScheme.primary 
                            else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("❤️", fontSize = 20.sp)
                        Text(
                            text = if (currentHeartRate != null) "완료" else "진행중",
                            fontSize = 12.sp,
                            color = if (currentHeartRate != null) 
                                MaterialTheme.colorScheme.primary 
                            else MaterialTheme.colorScheme.tertiary
                        )
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("📱", fontSize = 20.sp)
                        Text(
                            text = "대기",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // 심박수 측정 영역
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(280.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    when {
                        errorMessage != null -> {
                            Text(
                                text = errorMessage!!,
                                fontSize = 16.sp,
                                color = MaterialTheme.colorScheme.error,
                                textAlign = TextAlign.Center
                            )
                        }
                        hasPermissions == false -> {
                            Text(
                                text = "Health Connect 권한이 필요합니다",
                                fontSize = 16.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center
                            )
                        }
                        else -> {
                            // 안정시 심박수
                            restingHeartRate?.let { resting ->
                                Text(
                                    text = "안정시 심박수",
                                    fontSize = 14.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = "${resting.bpm} BPM",
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                            }
                            
                            // 현재 측정값
                            Text(
                                text = "❤️",
                                fontSize = 48.sp
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            
                            currentHeartRate?.let { current ->
                                Text(
                                    text = "${current.bpm} BPM",
                                    fontSize = 24.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                
                                // 차이 표시
                                restingHeartRate?.let { resting ->
                                    val difference = current.bpm - resting.bpm
                                    val differenceText = if (difference > 0) "+$difference" else "$difference"
                                    val differenceColor = when {
                                        difference > 10 -> MaterialTheme.colorScheme.error
                                        difference > 5 -> MaterialTheme.colorScheme.tertiary
                                        else -> MaterialTheme.colorScheme.onSurface
                                    }
                                    
                                    Text(
                                        text = "안정시 대비 $differenceText BPM",
                                        fontSize = 14.sp,
                                        color = differenceColor,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            } ?: run {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(
                                        text = "심박수를 측정하고 불러오기 버튼을 눌러주세요",
                                        fontSize = 16.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        textAlign = TextAlign.Center
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = "워치가 없다면 스킵할 수 있습니다",
                                        fontSize = 14.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        textAlign = TextAlign.Center
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // 버튼들
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // 뒤로가기 버튼
            OutlinedButton(
                onClick = onBackClick,
                enabled = !isLoading,
                modifier = Modifier.weight(1f)
            ) {
                Text("뒤로가기")
            }
            
            // 건너뛰기 버튼
            OutlinedButton(
                onClick = { onNextClick(null) },
                enabled = !isLoading,
                modifier = Modifier.weight(1f)
            ) {
                Text("건너뛰기")
            }
        }
        
        Spacer(modifier = Modifier.height(12.dp))
        
        // 메인 액션 버튼
        when {
            hasPermissions == false -> {
                Button(
                    onClick = {
                        permissionLauncher.launch(permissionHandler.healthPermissions)
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("권한 허용")
                }
            }
            currentHeartRate == null -> {
                Button(
                    onClick = {
                        scope.launch {
                            isLoading = true
                            try {
                                kotlinx.coroutines.delay(5000)
                                val heartRateData = permissionHandler.readRecentHeartRate()
                                currentHeartRate = heartRateData?.let {
                                    HeartRateData(
                                        bpm = it.bpm.toInt(),
                                        variability = 0.05f,
                                        measurementDuration = 30,
                                        timestamp = LocalDateTime.now()
                                    )
                                }
                            } catch (e: Exception) {
                                errorMessage = "측정 실패: ${e.message}"
                            } finally {
                                isLoading = false
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !isLoading && hasPermissions == true
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                    }
                    Text("불러오기")
                }
            }
        }
        
        Spacer(modifier = Modifier.weight(1f))
        
        // 다음 버튼 (측정 완료 시에만 표시)
        if (currentHeartRate != null) {
            Button(
                onClick = { onNextClick(currentHeartRate) },
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

private suspend fun loadRestingHeartRate(
    permissionHandler: PermissionHandler,
    onResult: (HeartRateData?) -> Unit
) {
    try {
        val restingAverage = permissionHandler.getRestingHeartRateAverage()
        if (restingAverage != null) {
            println("✅ 안정시 심박수 로드 성공: $restingAverage BPM")
            onResult(
                HeartRateData(
                    bpm = restingAverage.toInt(),
                    variability = 0.04f,
                    measurementDuration = 60,
                    timestamp = LocalDateTime.now()
                )
            )
        } else {
            println("⚠️ 안정시 심박수 데이터 없음, 기본값 사용")
            onResult(
                HeartRateData(
                    bpm = 70,
                    variability = 0.04f,
                    measurementDuration = 60,
                    timestamp = LocalDateTime.now()
                )
            )
        }
    } catch (e: Exception) {
        println("❌ 안정시 심박수 로드 실패: ${e.message}")
        onResult(
            HeartRateData(
                bpm = 70,
                variability = 0.04f,
                measurementDuration = 60,
                timestamp = LocalDateTime.now()
            )
        )
    }
}
