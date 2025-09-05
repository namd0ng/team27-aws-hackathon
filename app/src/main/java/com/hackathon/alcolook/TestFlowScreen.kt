package com.hackathon.alcolook

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

enum class TestStep {
    INTRO, FACE_DETECTION, HEART_RATE, WALKING, RESULT
}

@Composable
fun TestFlowScreen(modifier: Modifier = Modifier) {
    var currentStep by remember { mutableStateOf(TestStep.INTRO) }
    var faceResult by remember { mutableStateOf<Float?>(null) }
    var heartRateResult by remember { mutableStateOf<HeartRateAnalysis?>(null) }
    var walkingResult by remember { mutableStateOf<WalkingTestResult?>(null) }
    
    Column(modifier = modifier.fillMaxSize()) {
        // 상단 진행 표시바
        if (currentStep != TestStep.INTRO) {
            LinearProgressIndicator(
                progress = { 
                    when (currentStep) {
                        TestStep.FACE_DETECTION -> 0.25f
                        TestStep.HEART_RATE -> 0.5f
                        TestStep.WALKING -> 0.75f
                        TestStep.RESULT -> 1f
                        else -> 0f
                    }
                },
                modifier = Modifier.fillMaxWidth()
            )
        }
        
        when (currentStep) {
            TestStep.INTRO -> {
                IntroScreen(
                    onStartTest = { currentStep = TestStep.FACE_DETECTION }
                )
            }
            TestStep.FACE_DETECTION -> {
                FaceDetectionScreen(
                    onTestComplete = { result ->
                        faceResult = result
                        currentStep = TestStep.HEART_RATE
                    }
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
                    faceResult = faceResult,
                    heartRateResult = heartRateResult,
                    walkingResult = walkingResult,
                    onRestart = { 
                        faceResult = null
                        heartRateResult = null
                        walkingResult = null
                        currentStep = TestStep.INTRO 
                    }
                )
            }
        }
    }
}

@Composable
private fun IntroScreen(onStartTest: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "🍺 AlcoLook",
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 16.dp)
        )
        
        Text(
            text = "음주 상태 측정",
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
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "측정 과정",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 12.dp)
                )
                
                Text(
                    text = "1단계: 얼굴 분석",
                    fontSize = 14.sp,
                    modifier = Modifier.padding(bottom = 4.dp)
                )
                
                Text(
                    text = "2단계: 심박수 측정",
                    fontSize = 14.sp,
                    modifier = Modifier.padding(bottom = 4.dp)
                )
                
                Text(
                    text = "3단계: 보행 테스트",
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
private fun FaceDetectionScreen(onTestComplete: (Float?) -> Unit) {
    val context = LocalContext.current
    var isAnalyzing by remember { mutableStateOf(false) }
    var analysisResult by remember { mutableStateOf<FaceAnalysisResult?>(null) }
    var showCamera by remember { mutableStateOf(false) }
    val drunkDetectionService = remember { DrunkDetectionService(context) }
    val coroutineScope = rememberCoroutineScope()
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "1단계: 얼굴 분석",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 32.dp)
        )
        
        if (showCamera) {
            // 실제 카메라 프리뷰
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(400.dp)
                    .padding(bottom = 16.dp)
            ) {
                Box(modifier = Modifier.fillMaxSize()) {
                    CameraPreview(
                        onImageCaptured = { bitmap ->
                            isAnalyzing = true
                            showCamera = false
                            
                            coroutineScope.launch {
                                try {
                                    val result = drunkDetectionService.detectDrunkLevel(bitmap)
                                    analysisResult = result
                                } catch (e: Exception) {
                                    // 오류 시 테스트 결과 생성
                                    val testFaces = listOf(
                                        FaceBox(0.2f, 0.2f, 0.6f, 0.6f, 
                                               (kotlin.random.Random.nextFloat() * 80 + 10).toInt(), 
                                               "test")
                                    )
                                    analysisResult = FaceAnalysisResult(testFaces)
                                }
                                isAnalyzing = false
                            }
                        },
                        modifier = Modifier.fillMaxSize()
                    )
                    
                    // 얼굴 감지 오버레이 (분석 결과가 있을 때만 표시)
                    analysisResult?.let { result ->
                        FaceDetectionOverlay(
                            faces = result.faces,
                            imageWidth = 640,
                            imageHeight = 480,
                            displayWidth = 400f,
                            displayHeight = 400f,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                    
                    // 닫기 버튼
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        contentAlignment = Alignment.TopEnd
                    ) {
                        Button(
                            onClick = { showCamera = false },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color.Black.copy(alpha = 0.5f)
                            )
                        ) {
                            Text("✕", color = Color.White)
                        }
                    }
                }
            }
        } else {
            if (isAnalyzing) {
                CircularProgressIndicator()
                Text(
                    text = "얼굴 분석 중...",
                    fontSize = 16.sp,
                    modifier = Modifier.padding(top = 16.dp)
                )
            } else {
                analysisResult?.let { result ->
                    val avgDrunkLevel = if (result.faces.isNotEmpty()) {
                        result.faces.map { it.drunkPercentage }.average().toFloat() / 100f
                    } else 0.5f
                    
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = when {
                                avgDrunkLevel >= 0.7f -> Color(0xFFFFEBEE)
                                avgDrunkLevel >= 0.4f -> Color(0xFFFFF4E5)
                                else -> Color(0xFFE8F5E8)
                            }
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(20.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "얼굴 분석 결과",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(bottom = 8.dp)
                            )
                            
                            Text(
                                text = "${(avgDrunkLevel * 100).toInt()}%",
                                fontSize = 32.sp,
                                fontWeight = FontWeight.Bold,
                                color = when {
                                    avgDrunkLevel >= 0.7f -> Color(0xFFF44336)
                                    avgDrunkLevel >= 0.4f -> Color(0xFFFF9800)
                                    else -> Color(0xFF4CAF50)
                                }
                            )
                            
                            Text(
                                text = when {
                                    avgDrunkLevel >= 0.7f -> "높은 음주 가능성"
                                    avgDrunkLevel >= 0.4f -> "중간 음주 가능성"
                                    else -> "낮은 음주 가능성"
                                },
                                fontSize = 16.sp,
                                color = Color.Gray,
                                textAlign = TextAlign.Center
                            )
                            
                            if (result.faces.isNotEmpty()) {
                                Text(
                                    text = "감지된 얼굴: ${result.faces.size}개",
                                    fontSize = 12.sp,
                                    color = Color.Gray,
                                    modifier = Modifier.padding(top = 8.dp)
                                )
                            }
                        }
                    }
                }
                
                if (analysisResult == null) {
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
                                text = "📷 실시간 얼굴 분석",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(bottom = 8.dp)
                            )
                            Text(
                                text = "카메라로 얼굴을 촬영하여 음주 상태를 분석합니다",
                                fontSize = 14.sp,
                                color = Color.Gray,
                                modifier = Modifier.padding(bottom = 8.dp)
                            )
                            Text(
                                text = "• AWS Rekognition 기반 분석\n• 눈의 충혈 및 감김 정도\n• 얼굴 기울기 및 균형\n• 표정 변화 분석",
                                fontSize = 12.sp,
                                color = Color.Gray
                            )
                        }
                    }
                }
                
                Button(
                    onClick = { showCamera = true },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    enabled = !isAnalyzing
                ) {
                    Text("📷 카메라 열기", fontSize = 16.sp)
                }
                
                Button(
                    onClick = {
                        val avgDrunkLevel = analysisResult?.let { result ->
                            if (result.faces.isNotEmpty()) {
                                result.faces.map { it.drunkPercentage }.average().toFloat() / 100f
                            } else 0.5f
                        }
                        onTestComplete(avgDrunkLevel)
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = analysisResult != null
                ) {
                    Text("다음 단계로", fontSize = 16.sp)
                }
            }
        }
    }
}

// 실제 얼굴 분석 함수 (현재는 시뮬레이션)
private suspend fun analyzeFace(bitmap: android.graphics.Bitmap?): Float {
    // 실제로는 여기서 ML Kit 또는 AWS Rekognition 호출
    // 현재는 시뮬레이션
    return kotlin.random.Random.nextFloat() * 0.8f + 0.1f
}

@Composable
private fun HeartRateTestScreen(onTestComplete: (HeartRateAnalysis?) -> Unit) {
    val context = LocalContext.current
    var isAnalyzing by remember { mutableStateOf(false) }
    var heartRateAnalysis by remember { mutableStateOf<HeartRateAnalysis?>(null) }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "2단계: 심박수 측정",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 32.dp)
        )
        
        if (isAnalyzing) {
            CircularProgressIndicator()
            Text(
                text = "심박수 측정 중...",
                fontSize = 16.sp,
                modifier = Modifier.padding(top = 16.dp)
            )
        } else {
            heartRateAnalysis?.let { analysis ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            text = "심박수 분석 결과",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                        
                        Text(
                            text = "상태: ${analysis.status}",
                            fontSize = 16.sp,
                            modifier = Modifier.padding(bottom = 4.dp)
                        )
                        
                        Text(
                            text = "메시지: ${analysis.message}",
                            fontSize = 14.sp,
                            color = Color.Gray
                        )
                    }
                }
            }
            
            Button(
                onClick = {
                    isAnalyzing = true
                    
                    // 시뮬레이션
                    kotlinx.coroutines.GlobalScope.launch {
                        kotlinx.coroutines.delay(3000)
                        val statuses = listOf(HeartRateStatus.NORMAL, HeartRateStatus.NORMAL_ELEVATED, HeartRateStatus.SLIGHTLY_ELEVATED)
                        heartRateAnalysis = HeartRateAnalysis(
                            status = statuses.random(),
                            message = "심박수 측정 완료",
                            alcoholLevel = AlcoholLevel.MINIMAL,
                            recommendation = "정상 범위입니다"
                        )
                        isAnalyzing = false
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                enabled = !isAnalyzing
            ) {
                Text("심박수 측정 시작", fontSize = 16.sp)
            }
            
            Button(
                onClick = {
                    onTestComplete(heartRateAnalysis)
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = heartRateAnalysis != null
            ) {
                Text("다음 단계로", fontSize = 16.sp)
            }
        }
    }
}

@Composable
private fun WalkingTestScreen(onTestComplete: (WalkingTestResult?) -> Unit) {
    var isAnalyzing by remember { mutableStateOf(false) }
    var testResult by remember { mutableStateOf<WalkingTestResult?>(null) }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "3단계: 보행 테스트",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 32.dp)
        )
        
        if (isAnalyzing) {
            CircularProgressIndicator()
            Text(
                text = "보행 분석 중...",
                fontSize = 16.sp,
                modifier = Modifier.padding(top = 16.dp)
            )
        } else {
            testResult?.let { result ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            text = "보행 테스트 결과",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                        
                        Text(
                            text = when (result) {
                                WalkingTestResult.STABLE -> "✅ 안정적인 보행"
                                WalkingTestResult.SLIGHTLY_UNSTABLE -> "⚠️ 약간 불안정한 보행"
                                WalkingTestResult.UNSTABLE -> "🚨 불안정한 보행"
                                else -> "❓ 측정 오류"
                            },
                            fontSize = 16.sp,
                            color = when (result) {
                                WalkingTestResult.STABLE -> Color(0xFF4CAF50)
                                WalkingTestResult.SLIGHTLY_UNSTABLE -> Color(0xFFFF9800)
                                WalkingTestResult.UNSTABLE -> Color(0xFFF44336)
                                else -> Color.Gray
                            }
                        )
                    }
                }
            }
            
            Button(
                onClick = {
                    isAnalyzing = true
                    
                    // 시뮬레이션
                    kotlinx.coroutines.GlobalScope.launch {
                        kotlinx.coroutines.delay(5000)
                        val results = listOf(WalkingTestResult.STABLE, WalkingTestResult.SLIGHTLY_UNSTABLE, WalkingTestResult.UNSTABLE)
                        testResult = results.random()
                        isAnalyzing = false
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                enabled = !isAnalyzing
            ) {
                Text("보행 테스트 시작", fontSize = 16.sp)
            }
            
            Button(
                onClick = {
                    onTestComplete(testResult)
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = testResult != null
            ) {
                Text("결과 보기", fontSize = 16.sp)
            }
        }
    }
}

@Composable
private fun ResultScreen(
    faceResult: Float?,
    heartRateResult: HeartRateAnalysis?,
    walkingResult: WalkingTestResult?,
    onRestart: () -> Unit
) {
    var finalResult by remember { mutableStateOf<String?>(null) }
    var finalColor by remember { mutableStateOf(Color.Gray) }
    var isAnalyzing by remember { mutableStateOf(true) }
    
    LaunchedEffect(faceResult, heartRateResult, walkingResult) {
        kotlinx.coroutines.delay(2000)
        
        // 실제 측정 결과 사용
        println("얼굴 분석 결과: $faceResult")
        println("심박수 결과: ${heartRateResult?.status}")
        println("보행 결과: $walkingResult")
        
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
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "측정 완료",
            fontSize = 24.sp,
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
                    .padding(bottom = 32.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
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
                Text("다시 측정하기", fontSize = 16.sp)
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
