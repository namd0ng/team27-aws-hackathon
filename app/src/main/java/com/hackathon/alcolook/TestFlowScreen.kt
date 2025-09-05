package com.hackathon.alcolook

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
import kotlinx.coroutines.launch

@Composable
fun TestFlowScreen(modifier: Modifier = Modifier) {
    var currentStep by remember { mutableStateOf(TestStep.INTRO) }
    
    when (currentStep) {
        TestStep.INTRO -> {
            IntroScreen(
                onStartTest = { currentStep = TestStep.HEART_RATE }
            )
        }
        TestStep.HEART_RATE -> {
            HeartRateTestScreen(
                onTestComplete = { currentStep = TestStep.WALKING }
            )
        }
        TestStep.WALKING -> {
            WalkingTestScreen(
                onTestComplete = { currentStep = TestStep.RESULT }
            )
        }
        TestStep.RESULT -> {
            ResultScreen(
                onRestart = { currentStep = TestStep.INTRO }
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
            text = "음주 측정 테스트",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 32.dp)
        )
        
        Text(
            text = "2단계 테스트를 진행합니다:\n1. 심박수 측정\n2. 보행 테스트",
            fontSize = 16.sp,
            modifier = Modifier.padding(bottom = 32.dp)
        )
        
        Button(
            onClick = {
                println("테스트 시작 버튼 클릭")
                onStartTest()
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("테스트 시작", fontSize = 16.sp)
        }
    }
}

@Composable
private fun HeartRateTestScreen(onTestComplete: () -> Unit) {
    val context = LocalContext.current
    val permissionHandler = remember { PermissionHandler(context) }
    val scope = rememberCoroutineScope()
    
    var heartRateData by remember { mutableStateOf<HeartRateData?>(null) }
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
                        .padding(bottom = 16.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "측정된 심박수",
                            fontSize = 16.sp
                        )
                        Text(
                            text = "${data.bpm} BPM",
                            fontSize = 32.sp,
                            fontWeight = FontWeight.Bold
                        )
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
                    onTestComplete()
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("다음 단계로", fontSize = 16.sp)
            }
        }
    }
}

@Composable
private fun WalkingTestScreen(onTestComplete: () -> Unit) {
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
        
        Text(
            text = "보행 테스트를 시뮬레이션합니다",
            fontSize = 16.sp,
            modifier = Modifier.padding(bottom = 32.dp)
        )
        
        Button(
            onClick = {
                println("보행 테스트 완료 버튼 클릭")
                onTestComplete()
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("보행 테스트 완료", fontSize = 16.sp)
        }
    }
}

@Composable
private fun ResultScreen(onRestart: () -> Unit) {
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
                    text = "종합 판정",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "정상 - 음주 의심 없음",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
        }
        
        Button(
            onClick = {
                println("다시 측정 버튼 클릭")
                onRestart()
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("다시 측정", fontSize = 16.sp)
        }
    }
}

enum class TestStep {
    INTRO, HEART_RATE, WALKING, RESULT
}
