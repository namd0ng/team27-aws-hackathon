package com.hackathon.alcolook

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch

enum class TestStep {
    INTRO, HEART_RATE, WALKING, RESULT
}

@Composable
fun TestFlowScreen(modifier: Modifier = Modifier) {
    var currentStep by remember { mutableStateOf(TestStep.INTRO) }
    var heartRateResult by remember { mutableStateOf<HeartRateAnalysis?>(null) }
    var walkingResult by remember { mutableStateOf<WalkingTestResult?>(null) }
    
    when (currentStep) {
        TestStep.INTRO -> {
            IntroScreen(
                onStartTest = { currentStep = TestStep.HEART_RATE }
            )
        }
        TestStep.HEART_RATE -> {
            HeartRateTestScreen(
                onTestComplete = { result ->
                    heartRateResult = result
                    currentStep = TestStep.WALKING
                }
            )
        }
        TestStep.WALKING -> {
            WalkingTestScreen(
                onTestComplete = { result ->
                    walkingResult = result
                    currentStep = TestStep.RESULT
                }
            )
        }
        TestStep.RESULT -> {
            ResultScreen(
                heartRateResult = heartRateResult,
                walkingResult = walkingResult,
                onRestart = { 
                    heartRateResult = null
                    walkingResult = null
                    currentStep = TestStep.INTRO 
                }
            )
        }
    }
}

@Composable
private fun IntroScreen(onStartTest: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "AlcoLook",
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 16.dp)
        )
        
        Text(
            text = "음주 상태 측정 앱",
            fontSize = 18.sp,
            color = Color.Gray,
            modifier = Modifier.padding(bottom = 32.dp)
        )
        
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 32.dp)
        ) {
            Column(
                modifier = Modifier.padding(20.dp)
            ) {
                Text(
                    text = "측정 과정",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 12.dp)
                )
                
                Text(
                    text = "1단계: 심박수 측정",
                    fontSize = 14.sp,
                    modifier = Modifier.padding(bottom = 4.dp)
                )
                
                Text(
                    text = "2단계: 보행 테스트",
                    fontSize = 14.sp,
                    modifier = Modifier.padding(bottom = 12.dp)
                )
            }
        }
        
        Button(
            onClick = onStartTest,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("측정 시작", fontSize = 16.sp)
        }
    }
}

@Composable
private fun HeartRateTestScreen(onTestComplete: (HeartRateAnalysis?) -> Unit) {
    val context = LocalContext.current
    val permissionHandler = remember { PermissionHandler(context) }
    val scope = rememberCoroutineScope()
    
    var heartRateData by remember { mutableStateOf<HeartRateData?>(null) }
    var restingAverage by remember { mutableStateOf<Double?>(null) }
    var heartRateAnalysis by remember { mutableStateOf<HeartRateAnalysis?>(null) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var countdown by remember { mutableStateOf(0) }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        LinearProgressIndicator(
            progress = { 0.5f },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp)
        )
        
        Text(
            text = "1단계: 심박수 측정",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 32.dp)
        )
        
        if (isLoading) {
            CircularProgressIndicator()
            Text(
                text = if (countdown > 0) "데이터 동기화 중... $countdown" else "심박수 데이터를 가져오는 중...",
                modifier = Modifier.padding(top = 8.dp)
            )
        } else {
            // 초기 상태 또는 데이터 없을 때 안내 메시지
            if (heartRateData == null && errorMessage == null) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "워치에서 심박수를 측정해주세요",
                            fontSize = 16.sp,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                        Text(
                            text = "측정 후 아래 버튼을 눌러 데이터를 가져오세요",
                            fontSize = 14.sp,
                            color = Color.Gray
                        )
                    }
                }
            }
            
            heartRateData?.let { data ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = when (heartRateAnalysis?.status) {
                            HeartRateStatus.NORMAL -> Color(0xFFE8F5E8)
                            HeartRateStatus.NORMAL_ELEVATED -> Color(0xFFF0F8FF)
                            HeartRateStatus.SLIGHTLY_ELEVATED -> Color(0xFFFFF4E5)
                            HeartRateStatus.ELEVATED -> Color(0xFFFFEBEE)
                            HeartRateStatus.DANGEROUS -> Color(0xFFFFCDD2)
                            else -> Color.White
                        }
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "측정된 심박수",
                            fontSize = 18.sp,
                            color = Color.Gray,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                        
                        Row(
                            verticalAlignment = Alignment.Bottom,
                            modifier = Modifier.padding(bottom = 16.dp)
                        ) {
                            Text(
                                text = "${data.bpm}",
                                fontSize = 48.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.Black
                            )
                            Text(
                                text = " BPM",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Medium,
                                color = Color.Gray,
                                modifier = Modifier.padding(bottom = 4.dp)
                            )
                        }
                        
                        // 안정 시 심박수와 비교 표시
                        restingAverage?.let { average ->
                            HorizontalDivider(
                                modifier = Modifier.padding(vertical = 12.dp),
                                color = Color.LightGray
                            )
                            
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "안정 시 평균",
                                    fontSize = 14.sp,
                                    color = Color.Gray
                                )
                                Text(
                                    text = "${average.toInt()} BPM",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = Color.Gray
                                )
                            }
                            
                            val difference = data.bpm - average.toInt()
                            val percentageIncrease = ((difference / average) * 100).toInt()
                            val sign = if (difference > 0) "+" else ""
                            
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "차이",
                                    fontSize = 14.sp,
                                    color = Color.Gray
                                )
                                Text(
                                    text = "$sign$difference BPM ($sign$percentageIncrease%)",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (difference > 0) Color(0xFFF44336) else Color(0xFF4CAF50)
                                )
                            }
                        }
                        
                        // 상태 표시
                        heartRateAnalysis?.let { analysis ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 16.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = when (analysis.status) {
                                        HeartRateStatus.NORMAL -> Color(0xFFE8F5E8)
                                        HeartRateStatus.NORMAL_ELEVATED -> Color(0xFFF0F8FF)
                                        HeartRateStatus.SLIGHTLY_ELEVATED -> Color(0xFFFFF4E5)
                                        HeartRateStatus.ELEVATED -> Color(0xFFFFEBEE)
                                        HeartRateStatus.DANGEROUS -> Color(0xFFFFCDD2)
                                    }
                                )
                            ) {
                                Column(
                                    modifier = Modifier.padding(16.dp)
                                ) {
                                    Text(
                                        text = when (analysis.status) {
                                            HeartRateStatus.NORMAL -> "✓ ${analysis.message}"
                                            HeartRateStatus.NORMAL_ELEVATED -> "ℹ ${analysis.message}"
                                            HeartRateStatus.SLIGHTLY_ELEVATED -> "⚠ ${analysis.message}"
                                            HeartRateStatus.ELEVATED -> "⚠ ${analysis.message}"
                                            HeartRateStatus.DANGEROUS -> "🚨 ${analysis.message}"
                                        },
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = when (analysis.status) {
                                            HeartRateStatus.NORMAL -> Color(0xFF4CAF50)
                                            HeartRateStatus.NORMAL_ELEVATED -> Color(0xFF2196F3)
                                            HeartRateStatus.SLIGHTLY_ELEVATED -> Color(0xFFFF9800)
                                            HeartRateStatus.ELEVATED -> Color(0xFFF44336)
                                            HeartRateStatus.DANGEROUS -> Color(0xFFD32F2F)
                                        },
                                        modifier = Modifier.padding(bottom = 8.dp)
                                    )
                                    
                                    Text(
                                        text = "음주 수준: ${
                                            when (analysis.alcoholLevel) {
                                                AlcoholLevel.NONE -> "없음"
                                                AlcoholLevel.MINIMAL -> "최소"
                                                AlcoholLevel.LOW -> "경미"
                                                AlcoholLevel.MODERATE -> "중간 (소주 1-2병)"
                                                AlcoholLevel.HIGH -> "과도"
                                            }
                                        }",
                                        fontSize = 14.sp,
                                        color = Color.Gray,
                                        modifier = Modifier.padding(bottom = 4.dp)
                                    )
                                    
                                    Text(
                                        text = "권장사항: ${analysis.recommendation}",
                                        fontSize = 14.sp,
                                        color = Color.Gray
                                    )
                                }
                            }
                        }
                    }
                }
            }
            
            errorMessage?.let { error ->
                Text(
                    text = "오류: $error",
                    color = Color.Red,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
            }
            
            Button(
                onClick = {
                    scope.launch {
                        isLoading = true
                        errorMessage = null
                        try {
                            // 5초 카운트다운
                            for (i in 5 downTo 1) {
                                countdown = i
                                kotlinx.coroutines.delay(1000)
                            }
                            countdown = 0
                            
                            val data = permissionHandler.readRecentHeartRate()
                            if (data != null) {
                                heartRateData = data
                                
                                // 안정 시 심박수 평균 조회
                                val average = permissionHandler.getRestingHeartRateAverage()
                                restingAverage = average
                                
                                // 심박수 상태 분석
                                heartRateAnalysis = analyzeHeartRateStatus(data.bpm, average)
                            } else {
                                errorMessage = "심박수 데이터를 찾을 수 없습니다. 워치에서 심박수를 측정했는지 확인해주세요."
                            }
                        } catch (e: Exception) {
                            errorMessage = e.message
                        } finally {
                            isLoading = false
                            countdown = 0
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                enabled = !isLoading
            ) {
                Text("심박수 데이터 가져오기", fontSize = 16.sp)
            }
            
            Button(
                onClick = {
                    println("심박수 측정 완료 버튼 클릭")
                    onTestComplete(heartRateAnalysis)
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("다음 단계로", fontSize = 16.sp)
            }
        }
    }
}

@Composable
private fun WalkingTestScreen(onTestComplete: (WalkingTestResult?) -> Unit) {
    val context = LocalContext.current
    val gyroscopeManager = remember { GyroscopeManager(context) }
    
    var isTestStarted by remember { mutableStateOf(false) }
    var isTestRunning by remember { mutableStateOf(false) }
    var testResult by remember { mutableStateOf<WalkingTestResult?>(null) }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        LinearProgressIndicator(
            progress = { 1f },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp)
        )
        
        Text(
            text = "2단계: 보행 테스트",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 32.dp)
        )
        
        if (!isTestStarted) {
            // 초기 상태 - 측정 안내
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "보행 테스트 준비",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )
                    Text(
                        text = "힐 투 토우(Heel to Toe) 보행법",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1976D2),
                        modifier = Modifier.padding(bottom = 12.dp)
                    )
                    
                    // 보행법 설명 이미지
                    Image(
                        painter = painterResource(id = R.drawable.heel_to_toe_guide),
                        contentDescription = "힐 투 토우 보행법 안내",
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp)
                            .padding(vertical = 12.dp)
                    )
                    Text(
                        text = "• 양손을 어깨 높이로 올리세요\n• 한 발의 뒤꿈치를 다른 발의 발가락에 붙여서 걸으세요\n• 직선으로 10걸음 → 돌기 → 10걸음",
                        fontSize = 14.sp,
                        color = Color.Gray,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )
                    Text(
                        text = "폰을 손에 들고 측정하세요",
                        fontSize = 14.sp,
                        color = Color.Gray
                    )
                }
            }
            
            Button(
                onClick = {
                    isTestStarted = true
                    isTestRunning = true
                    gyroscopeManager.startRecording { result ->
                        testResult = result
                        isTestRunning = false
                    }
                    println("자이로스코프 측정 시작")
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("측정 시작", fontSize = 16.sp)
            }
        } else if (isTestRunning) {
            // 측정 중 상태
            CircularProgressIndicator()
            Text(
                text = "보행 측정 중...",
                fontSize = 16.sp,
                modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
            )
            Text(
                text = "최소 10걸음 이상 걸어주세요",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1976D2),
                modifier = Modifier.padding(bottom = 4.dp)
            )
            Text(
                text = "• 양손을 어깨 높이로 올리세요\n• 뒤꿈치를 발가락에 붙여서 걸으세요\n• 균형을 유지하며 천천히 걸으세요",
                fontSize = 12.sp,
                color = Color.Gray,
                modifier = Modifier.padding(bottom = 32.dp)
            )
            
            Button(
                onClick = {
                    gyroscopeManager.stopRecording()
                    println("자이로스코프 측정 완료")
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("측정 완료", fontSize = 16.sp)
            }
        } else {
            // 측정 완료 상태
            testResult?.let { result ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = when (result) {
                            WalkingTestResult.STABLE -> Color(0xFFE8F5E8)
                            WalkingTestResult.SLIGHTLY_UNSTABLE -> Color(0xFFFFF4E5)
                            WalkingTestResult.UNSTABLE -> Color(0xFFFFEBEE)
                            else -> Color.White
                        }
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "보행 분석 결과",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.Black,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                        Text(
                            text = when (result) {
                                WalkingTestResult.STABLE -> "정상 - 안정적인 보행"
                                WalkingTestResult.SLIGHTLY_UNSTABLE -> "주의 - 약간 불안정한 보행"
                                WalkingTestResult.UNSTABLE -> "위험 - 불안정한 보행"
                                WalkingTestResult.INSUFFICIENT_DATA -> "데이터 부족 - 다시 측정하세요"
                                WalkingTestResult.ERROR -> "센서 오류"
                            },
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = when (result) {
                                WalkingTestResult.STABLE -> Color(0xFF4CAF50)
                                WalkingTestResult.SLIGHTLY_UNSTABLE -> Color(0xFFFF9800)
                                WalkingTestResult.UNSTABLE -> Color(0xFFF44336)
                                else -> Color.Black
                            }
                        )
                    }
                }
            }
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (testResult == WalkingTestResult.INSUFFICIENT_DATA || testResult == WalkingTestResult.ERROR) {
                    Button(
                        onClick = {
                            isTestStarted = false
                            testResult = null
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("다시 측정", fontSize = 16.sp)
                    }
                }
                
                Button(
                    onClick = {
                        println("보행 테스트 완료 버튼 클릭")
                        onTestComplete(testResult)
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("다음 단계로", fontSize = 16.sp)
                }
            }
        }
    }
}

@Composable
private fun ResultScreen(
    heartRateResult: HeartRateAnalysis?,
    walkingResult: WalkingTestResult?,
    onRestart: () -> Unit
) {
    var finalResult by remember { mutableStateOf<String?>(null) }
    var finalColor by remember { mutableStateOf(Color.Gray) }
    var isAnalyzing by remember { mutableStateOf(true) }
    
    LaunchedEffect(heartRateResult, walkingResult) {
        kotlinx.coroutines.delay(2000)
        
        // 실제 측정 결과 사용
        println("실제 심박수 결과: ${heartRateResult?.status}")
        println("실제 보행 결과: $walkingResult")
        
        // 최종 결과 계산
        val finalAssessment = calculateFinalResult(heartRateResult?.status, walkingResult)
        finalResult = finalAssessment.first
        finalColor = finalAssessment.second
        
        println("최종 결과: $finalResult")
        
        isAnalyzing = false
    }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "측정 결과",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 32.dp)
        )
        
        if (isAnalyzing) {
            CircularProgressIndicator()
            Text(
                text = "결과 분석 중...",
                fontSize = 16.sp,
                modifier = Modifier.padding(top = 16.dp)
            )
        } else {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 32.dp),
                colors = CardDefaults.cardColors(
                    containerColor = finalColor.copy(alpha = 0.1f)
                )
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "종합 판정",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = finalResult ?: "분석 중",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = finalColor,
                        modifier = Modifier.padding(vertical = 16.dp)
                    )
                }
            }
            
            Button(
                onClick = onRestart,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("다시 측정", fontSize = 16.sp)
            }
        }
    }
}

// 최종 결과 계산 함수 (단순화된 로직)
private fun calculateFinalResult(
    heartRateStatus: HeartRateStatus?,
    walkingResult: WalkingTestResult?
): Pair<String, Color> {
    
    // 보행 테스트가 가장 중요 (85% 가중치)
    return when (walkingResult) {
        WalkingTestResult.UNSTABLE -> {
            println("보행 불안정 -> 위험")
            Pair("🚨 위험 - 음주 상태", Color(0xFFD32F2F))
        }
        WalkingTestResult.SLIGHTLY_UNSTABLE -> {
            println("보행 약간 불안정 -> 주의")
            Pair("⚠️ 주의 - 음주 의심", Color(0xFFFF9800))
        }
        WalkingTestResult.STABLE -> {
            // 보행이 안정적이면 심박수 확인
            when (heartRateStatus) {
                HeartRateStatus.DANGEROUS -> {
                    println("보행 안정 + 심박수 위험 -> 주의")
                    Pair("⚠️ 주의 - 심박수 이상", Color(0xFFFF9800))
                }
                HeartRateStatus.ELEVATED -> {
                    println("보행 안정 + 심박수 상승 -> 경미")
                    Pair("ℹ️ 경미한 반응", Color(0xFF2196F3))
                }
                else -> {
                    println("보행 안정 + 심박수 정상 -> 정상")
                    Pair("✅ 정상 범위", Color(0xFF4CAF50))
                }
            }
        }
        else -> {
            println("데이터 부족 -> 경미")
            Pair("ℹ️ 데이터 부족", Color(0xFF2196F3))
        }
    }
}
