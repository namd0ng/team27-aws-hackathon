package com.hackathon.alcolook

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun WalkingTestScreen(
    modifier: Modifier = Modifier,
    onBack: (() -> Unit)? = null,
    viewModel: WalkingTestViewModel? = null
) {
    val context = LocalContext.current
    val walkingViewModel = viewModel ?: viewModel { WalkingTestViewModel(context) }
    val uiState by walkingViewModel.uiState.collectAsState()
    val walkingData = uiState.walkingData
    
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "보행 테스트",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 32.dp)
        )
        
        onBack?.let {
            Button(
                onClick = it,
                modifier = Modifier.padding(bottom = 16.dp)
            ) {
                Text("← 메인으로")
            }
        }
        
        // 현재 단계 표시
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = getPhaseColor(walkingData.phase)
            )
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = getPhaseText(walkingData.phase),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                
                if (walkingData.isRecording) {
                    Text(
                        text = "시간: ${walkingData.duration / 1000}초",
                        fontSize = 14.sp,
                        color = Color.White,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                    
                    Text(
                        text = "불안정성: ${"%.2f".format(walkingData.currentInstability)}",
                        fontSize = 14.sp,
                        color = Color.White
                    )
                }
            }
        }
        
        // 점수 표시 (테스트 완료 시)
        if (walkingData.phase == TestPhase.COMPLETED) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "테스트 점수",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = "${walkingData.score}점",
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold,
                        color = getScoreColor(walkingData.score)
                    )
                    
                    Text(
                        text = "음주 수준: ${getAlcoholLevelText(uiState.alcoholLevel)}",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = getAlcoholLevelColor(uiState.alcoholLevel),
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
            }
        }
        
        // 테스트 지침
        if (!walkingData.isRecording && walkingData.phase == TestPhase.READY) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F5F5))
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "테스트 방법:",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "1. 양손을 어깨 높이로 올리세요\n2. 일직선으로 10걸음 걸으세요\n3. 180도 돌아서 다시 10걸음 걸으세요",
                        fontSize = 14.sp,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
            }
        }
        
        // 버튼들
        Row(
            modifier = Modifier.padding(top = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            when (walkingData.phase) {
                TestPhase.READY -> {
                    Button(
                        onClick = { walkingViewModel.startTest() },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("테스트 시작")
                    }
                }
                TestPhase.FIRST_10_STEPS, TestPhase.TURN, TestPhase.SECOND_10_STEPS -> {
                    Button(
                        onClick = { walkingViewModel.nextPhase() },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("다음 단계")
                    }
                    Button(
                        onClick = { walkingViewModel.stopTest() },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
                    ) {
                        Text("테스트 종료")
                    }
                }
                TestPhase.COMPLETED -> {
                    Button(
                        onClick = { 
                            // 테스트 재시작 로직
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("다시 테스트")
                    }
                }
            }
        }
    }
}

@Composable
private fun getPhaseColor(phase: TestPhase): Color {
    return when (phase) {
        TestPhase.READY -> Color.Gray
        TestPhase.FIRST_10_STEPS -> Color.Blue
        TestPhase.TURN -> Color(0xFFFF9800)
        TestPhase.SECOND_10_STEPS -> Color.Blue
        TestPhase.COMPLETED -> Color.Green
    }
}

private fun getPhaseText(phase: TestPhase): String {
    return when (phase) {
        TestPhase.READY -> "준비"
        TestPhase.FIRST_10_STEPS -> "첫 번째 10걸음"
        TestPhase.TURN -> "턴"
        TestPhase.SECOND_10_STEPS -> "두 번째 10걸음"
        TestPhase.COMPLETED -> "완료"
    }
}

@Composable
private fun getScoreColor(score: Int): Color {
    return when {
        score >= 80 -> Color.Green
        score >= 60 -> Color(0xFFFF9800)
        score >= 40 -> Color(0xFFFF5722)
        else -> Color.Red
    }
}
