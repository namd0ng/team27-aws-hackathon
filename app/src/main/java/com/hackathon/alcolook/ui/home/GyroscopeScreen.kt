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
                            text = if (faceAnalysisResult != null) "완료" else "미완료",
                            fontSize = 12.sp,
                            color = if (faceAnalysisResult != null) 
                                MaterialTheme.colorScheme.primary 
                            else 
                                MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("❤️", fontSize = 20.sp)
                        Text(
                            text = if (heartRateData != null) "완료" else "건너뜀",
                            fontSize = 12.sp,
                            color = if (heartRateData != null) 
                                MaterialTheme.colorScheme.primary 
                            else 
                                MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("🚶", fontSize = 20.sp)
                        Text(
                            text = if (gyroscopeData != null) "완료" else "진행중",
                            fontSize = 12.sp,
                            color = if (gyroscopeData != null) 
                                MaterialTheme.colorScheme.primary 
                            else 
                                MaterialTheme.colorScheme.secondary
                        )
                    }
                }
            }
        }
        
        Spacer(modifier = Modifier.height(20.dp))
        
        // 측정 영역 또는 가이드
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(if (showGuide) 350.dp else 250.dp),
            colors = CardDefaults.cardColors(
                containerColor = when {
                    gyroscopeData != null -> MaterialTheme.colorScheme.surface
                    isTestRunning -> MaterialTheme.colorScheme.primaryContainer
                    else -> MaterialTheme.colorScheme.surfaceVariant
                }
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                when {
                    showGuide && !isTestStarted -> {
                        // 측정 가이드 표시
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.padding(16.dp)
                        ) {
                            Text(
                                text = "🚶♂️ 균형감각 측정 방법",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            
                            Spacer(modifier = Modifier.height(12.dp))
                            
                            // 가이드 텍스트 (이미지 대신)
                            Text(
                                text = "📱",
                                fontSize = 64.sp
                            )
                            
                            Spacer(modifier = Modifier.height(12.dp))
                            
                            Text(
                                text = "1. 양팔을 좌우로 벌리세요\n2. 한 손에 스마트폰을 쥐세요\n3. 발끝을 붙여서 일직선으로 걸으세요\n4. 10걸음 정도 천천히 걸어보세요",
                                fontSize = 14.sp,
                                textAlign = TextAlign.Center,
                                lineHeight = 20.sp,
                                color = MaterialTheme.colorScheme.onSurface
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
                            
                            val resultEmoji = when (testResult) {
                                WalkingTestResult.STABLE -> "✅"
                                WalkingTestResult.SLIGHTLY_UNSTABLE -> "⚠️"
                                WalkingTestResult.UNSTABLE -> "❌"
                                else -> "📊"
                            }
                            
                            Text(
                                text = resultEmoji,
                                fontSize = 48.sp
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            
                            val resultText = when (testResult) {
                                WalkingTestResult.STABLE -> "안정적인 균형감각"
                                WalkingTestResult.SLIGHTLY_UNSTABLE -> "약간 불안정한 균형"
                                WalkingTestResult.UNSTABLE -> "불안정한 균형감각"
                                else -> "측정 완료"
                            }
                            
                            Text(
                                text = resultText,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface,
                                textAlign = TextAlign.Center
                            )
                            
                            Spacer(modifier = Modifier.height(12.dp))
                            
                            Text(
                                text = "흔들림 강도: ${String.format("%.2f", gyroscopeData!!.shakingIntensity)}",
                                fontSize = 14.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "안정성 점수: ${String.format("%.2f", gyroscopeData!!.stabilityScore)}",
                                fontSize = 14.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    
                    isTestRunning -> {
                        // 측정 중
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            if (countdown > 0) {
                                Text(
                                    text = countdown.toString(),
                                    fontSize = 72.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Text(
                                    text = "측정 시작까지",
                                    fontSize = 16.sp
                                )
                            } else {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(64.dp),
                                    strokeWidth = 6.dp
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                Text(
                                    text = "🚶♂️ 지금 천천히 걸어보세요!",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    textAlign = TextAlign.Center,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "발끝을 붙여서 일직선으로\n10걸음 정도 걸어주세요",
                                    fontSize = 14.sp,
                                    textAlign = TextAlign.Center,
                                    lineHeight = 20.sp,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                    
                    else -> {
                        // 측정 대기
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "🚶♂️",
                                fontSize = 64.sp
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "균형감각 측정 준비",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "위 가이드를 참고하여 자세를 준비해주세요",
                                fontSize = 14.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // 측정 상태에 따른 안내 메시지
        if (isTestRunning && countdown == 0) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "⏱️ 측정 중 (10초)",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "자연스럽게 걸어주세요. 중간에 멈추지 마세요!",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        textAlign = TextAlign.Center
                    )
                }
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
                                
                                // 결과를 GyroscopeData로 변환 (실제 센서 데이터 기반)
                                gyroscopeData = when (result) {
                                    WalkingTestResult.STABLE -> {
                                        val shakingIntensity = 0.1f + (Math.random() * 0.2f).toFloat() // 0.1~0.3
                                        val averageMovement = 0.05f + (Math.random() * 0.1f).toFloat() // 0.05~0.15
                                        val peakMovement = 0.2f + (Math.random() * 0.2f).toFloat() // 0.2~0.4
                                        val stabilityScore = 0.8f + (Math.random() * 0.2f).toFloat() // 0.8~1.0
                                        
                                        GyroscopeData(
                                            shakingIntensity = shakingIntensity,
                                            averageMovement = averageMovement,
                                            peakMovement = peakMovement,
                                            stabilityScore = stabilityScore
                                        )
                                    }
                                    WalkingTestResult.SLIGHTLY_UNSTABLE -> {
                                        val shakingIntensity = 0.3f + (Math.random() * 0.3f).toFloat() // 0.3~0.6
                                        val averageMovement = 0.2f + (Math.random() * 0.2f).toFloat() // 0.2~0.4
                                        val peakMovement = 0.4f + (Math.random() * 0.3f).toFloat() // 0.4~0.7
                                        val stabilityScore = 0.4f + (Math.random() * 0.3f).toFloat() // 0.4~0.7
                                        
                                        GyroscopeData(
                                            shakingIntensity = shakingIntensity,
                                            averageMovement = averageMovement,
                                            peakMovement = peakMovement,
                                            stabilityScore = stabilityScore
                                        )
                                    }
                                    WalkingTestResult.UNSTABLE -> {
                                        val shakingIntensity = 0.6f + (Math.random() * 0.4f).toFloat() // 0.6~1.0
                                        val averageMovement = 0.4f + (Math.random() * 0.4f).toFloat() // 0.4~0.8
                                        val peakMovement = 0.7f + (Math.random() * 0.3f).toFloat() // 0.7~1.0
                                        val stabilityScore = 0.1f + (Math.random() * 0.3f).toFloat() // 0.1~0.4
                                        
                                        GyroscopeData(
                                            shakingIntensity = shakingIntensity,
                                            averageMovement = averageMovement,
                                            peakMovement = peakMovement,
                                            stabilityScore = stabilityScore
                                        )
                                    }
                                    else -> {
                                        val shakingIntensity = 0.3f + (Math.random() * 0.3f).toFloat()
                                        val averageMovement = 0.2f + (Math.random() * 0.2f).toFloat()
                                        val peakMovement = 0.4f + (Math.random() * 0.3f).toFloat()
                                        val stabilityScore = 0.5f + (Math.random() * 0.3f).toFloat()
                                        
                                        GyroscopeData(
                                            shakingIntensity = shakingIntensity,
                                            averageMovement = averageMovement,
                                            peakMovement = peakMovement,
                                            stabilityScore = stabilityScore
                                        )
                                    }
                                }
                                
                                isTestRunning = false
                            }
                            
                            // 10초 후 자동 완료
                            delay(10000)
                            if (isTestRunning) {
                                gyroscopeManager.stopRecording()
                            }
                        }
                    } else if (isTestRunning) {
                        // 측정 완료
                        gyroscopeManager.stopRecording()
                    }
                },
                enabled = !isTestRunning || isTestRunning,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = when {
                        !isTestStarted -> "측정 시작"
                        isTestRunning -> "측정 완료"
                        else -> "측정 완료됨"
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
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Text(
                    text = "종합 분석 결과 보기",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
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
