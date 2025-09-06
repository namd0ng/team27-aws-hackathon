package com.hackathon.alcolook.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.hackathon.alcolook.data.IntoxicationLevel
import com.hackathon.alcolook.viewmodel.IntoxicationAnalysisViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnalysisScreen() {
    val context = LocalContext.current
    val viewModel: IntoxicationAnalysisViewModel = remember { IntoxicationAnalysisViewModel(context) }
    val uiState by viewModel.uiState.collectAsState()
    
    // 에러 스낵바
    uiState.error?.let { error ->
        LaunchedEffect(error) {
            // TODO: 스낵바 표시
            viewModel.clearError()
        }
    }
    
    // 저장 완료 스낵바
    if (uiState.isSaved) {
        LaunchedEffect(uiState.isSaved) {
            // TODO: 저장 완료 스낵바 표시
            viewModel.clearSavedState()
        }
    }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        if (uiState.isLoading) {
            LoadingScreen()
        } else if (uiState.report != null) {
            IntoxicationResultScreen(
                report = uiState.report!!,
                onShare = { viewModel.shareResult() },
                onSave = { viewModel.saveToCalendar() },
                onRetry = { viewModel.retryAnalysis() }
            )
        } else {
            StartAnalysisScreen(
                onStartAnalysis = { level -> viewModel.startTestAnalysis(level) }
            )
        }
    }
}

@Composable
private fun LoadingScreen() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            CircularProgressIndicator()
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "AI가 센서 데이터를 분석하고 있습니다...",
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun StartAnalysisScreen(
    onStartAnalysis: (IntoxicationLevel?) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        item {
            Text(
                text = "🍺 AlcoLook",
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
        }
        
        item {
            Text(
                text = "3가지 센서를 활용한 음주 상태 분석",
                style = MaterialTheme.typography.titleMedium,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        
        item {
            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "📊 분석 요소",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    listOf(
                        "👤 얼굴 인식 - 눈 상태, 입 상태, 얼굴 기울기",
                        "❤️ 심박수 측정 - BPM, 심박 변이도",
                        "📱 자이로센서 - 흔들림, 안정성 점수"
                    ).forEach { item ->
                        Text(
                            text = "• $item",
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(bottom = 4.dp)
                        )
                    }
                }
            }
        }
        
        item {
            Text(
                text = "테스트 분석 시작",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
        }
        
        item {
            Button(
                onClick = { onStartAnalysis(null) },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("🎲 랜덤 데이터로 분석")
            }
        }
        
        item {
            Text(
                text = "특정 상태로 테스트",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        
        items(IntoxicationLevel.values()) { level ->
            val (emoji, text, description) = when (level) {
                IntoxicationLevel.NORMAL -> Triple("😊", "정상", "정상적인 상태")
                IntoxicationLevel.SLIGHTLY -> Triple("😵‍💫", "조금 취함", "약간의 음주 상태")
                IntoxicationLevel.MODERATE -> Triple("🥴", "적당히 취함", "중간 정도 음주 상태")
                IntoxicationLevel.HEAVY -> Triple("🤢", "과음", "심한 음주 상태")
            }
            
            OutlinedButton(
                onClick = { onStartAnalysis(level) },
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("$emoji $text")
                    Text(
                        text = description,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
        
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "⚠️ 면책 고지",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Text(
                        text = "본 앱의 분석 결과는 의료 목적이 아니며, 실제 운전 가능 여부를 판단하는 기준으로 사용할 수 없습니다.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )
                }
            }
        }
    }
}
