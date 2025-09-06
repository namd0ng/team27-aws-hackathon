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
import com.hackathon.alcolook.data.GyroscopeData
import com.hackathon.alcolook.GyroscopeManager
import com.hackathon.alcolook.WalkingTestResult
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GyroscopeScreen(
    faceAnalysisResult: Float?, // 얼굴 분석 결과
    heartRateData: com.hackathon.alcolook.data.HeartRateData?, // 심박수 데이터
    onNextClick: (GyroscopeData?) -> Unit,
    onBackClick: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val gyroscopeManager = remember { GyroscopeManager(context) }
    
    var gyroscopeData by remember { mutableStateOf<GyroscopeData?>(null) }
    var isTestStarted by remember { mutableStateOf(false) }
    var isTestRunning by remember { mutableStateOf(false) }
    var testResult by remember { mutableStateOf<WalkingTestResult?>(null) }
    var countdown by remember { mutableStateOf(0) }
    var showGuide by remember { mutableStateOf(true) }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(20.dp))
        
        // 제목
        Text(
            text = "균형감각 측정 (선택)",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // 이전 측정 결과 요약
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
                            text = if (heartRateData != null) "완료" else "대기",
                            fontSize = 12.sp,
                            color = if (heartRateData != null) 
                                MaterialTheme.colorScheme.primary 
                            else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("📱", fontSize = 20.sp)
                        Text(
                            text = if (gyroscopeData != null) "완료" else "진행중",
                            fontSize = 12.sp,
                            color = if (gyroscopeData != null) 
                                MaterialTheme.colorScheme.primary 
                            else MaterialTheme.colorScheme.tertiary
                        )
                    }
                }
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // 메인 측정 영역
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
                when {
                    showGuide && !isTestStarted -> {
                        // 가이드 화면
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "균형감각 측정 방법",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            
                            Spacer(modifier = Modifier.height(12.dp))
                            
                            // 가이드 텍스트 (이미지 대신)
                            Text(
                                text = "📱",
                                fontSize = 64.sp
                            )
                            
                            Spacer(modifier = Modifier.height(16.dp))
                            
                            Text(
                                text = "1. 핸드폰을 손에 들고 서세요",
                                fontSize = 14.sp,
                                textAlign = TextAlign.Center
                            )
                            Text(
                                text = "2. 눈을 감고 10초간 제자리에 서세요",
                                fontSize = 14.sp,
                                textAlign = TextAlign.Center
                            )
                            Text(
                                text = "3. 흔들림이 적을수록 좋은 결과입니다",
                                fontSize = 14.sp,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                    
                    countdown > 0 -> {
                        // 카운트다운
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "준비하세요",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = countdown.toString(),
                                fontSize = 72.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                    
                    isTestRunning -> {
                        // 측정 중
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "측정 중...",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            CircularProgressIndicator(
                                modifier = Modifier.size(48.dp),
                                strokeWidth = 4.dp
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "눈을 감고 제자리에 서세요",
                                fontSize = 14.sp,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                    
                    gyroscopeData != null -> {
                        // 측정 완료 결과 표시
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "측정 완료됨!",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            
                            Spacer(modifier = Modifier.height(16.dp))
                            
                            Text(
                                text = "📊",
                                fontSize = 48.sp
                            )
                            
                            Spacer(modifier = Modifier.height(8.dp))
                            
                            Text(
                                text = "흔들림 강도: ${String.format("%.2f", gyroscopeData?.shakingIntensity ?: 0f)}",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Medium
                            )
                            
                            Text(
                                text = "안정성 점수: ${String.format("%.1f", (gyroscopeData?.stabilityScore ?: 0f) * 100)}%",
                                fontSize = 14.sp
                            )
                            
                            testResult?.let { result ->
                                val resultText = when (result) {
                                    WalkingTestResult.STABLE -> "안정적인 균형감각"
                                    WalkingTestResult.SLIGHTLY_UNSTABLE -> "약간 불안정한 균형"
                                    WalkingTestResult.UNSTABLE -> "불안정한 균형감각"
                                    else -> "측정 완료"
                                }
                                
                                Text(
                                    text = resultText,
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
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
                enabled = !isTestRunning,
                modifier = Modifier.weight(1f)
            ) {
                Text("뒤로가기")
            }
            
            // 건너뛰기 버튼
            OutlinedButton(
                onClick = { 
                    onNextClick(null)
                },
                enabled = !isTestRunning,
                modifier = Modifier.weight(1f)
            ) {
                Text("건너뛰기")
            }
        }
        
        Spacer(modifier = Modifier.height(12.dp))
        
        // 측정 시작 버튼 (측정 완료 시 숨김)
        if (gyroscopeData == null) {
            Button(
                onClick = {
                    if (!isTestStarted && !isTestRunning) {
                        // 측정 시작
                        showGuide = false
                        isTestStarted = true
                        countdown = 3
                        
                        scope.launch {
                            // 3초 카운트다운
                            repeat(3) {
                                delay(1000)
                                countdown--
                            }
                            
                            // 측정 시작
                            isTestRunning = true
                            gyroscopeManager.startRecording { result ->
                                testResult = result
                                gyroscopeData = com.hackathon.alcolook.data.GyroscopeData(
                                    shakingIntensity = when (result) {
                                        WalkingTestResult.STABLE -> 0.1f
                                        WalkingTestResult.SLIGHTLY_UNSTABLE -> 0.5f
                                        WalkingTestResult.UNSTABLE -> 0.9f
                                        else -> 0.3f
                                    },
                                    stabilityScore = when (result) {
                                        WalkingTestResult.STABLE -> 0.9f
                                        WalkingTestResult.SLIGHTLY_UNSTABLE -> 0.6f
                                        WalkingTestResult.UNSTABLE -> 0.3f
                                        else -> 0.5f
                                    },
                                    averageMovement = 0.2f,
                                    peakMovement = 0.8f
                                )
                                isTestRunning = false
                            }
                            
                            // 10초 후 자동 정지
                            delay(10000)
                            if (isTestRunning) {
                                gyroscopeManager.stopRecording()
                            }
                        }
                    } else if (isTestRunning) {
                        // 측정 중단
                        gyroscopeManager.stopRecording()
                        isTestRunning = false
                    }
                },
                enabled = !isTestRunning || isTestRunning,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = when {
                        !isTestStarted -> "측정 시작"
                        isTestRunning -> "측정 중"
                        else -> "측정 준비 중"
                    },
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
        
        // 측정 완료 시 다음 버튼
        if (gyroscopeData != null) {
            Spacer(modifier = Modifier.height(12.dp))
            Button(
                onClick = { onNextClick(gyroscopeData) },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("다음으로")
            }
        }
        
        Spacer(modifier = Modifier.weight(1f))
        
        // 면책 고지
        Text(
            text = "⚠️ 본 측정 결과는 의료 목적이 아니며, 운전 판단에 사용하지 마세요.",
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}
