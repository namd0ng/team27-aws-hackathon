package com.hackathon.alcolook.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hackathon.alcolook.data.*
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IntoxicationResultScreen(
    report: IntoxicationReport,
    onShare: () -> Unit = {},
    onSave: () -> Unit = {},
    onRetry: () -> Unit = {}
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 제목
        item {
            Text(
                text = "음주 상태 분석 결과",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center
            )
        }
        
        // 최종 결과 카드
        item {
            ResultSummaryCard(report)
        }
        
        // 센서 데이터 상세
        item {
            SensorDataCard(report.sensorData)
        }
        
        // AI 분석 보고서
        item {
            AnalysisReportCard(report)
        }
        
        // 권장사항
        item {
            RecommendationsCard(report.recommendations)
        }
        
        // 면책 고지
        item {
            DisclaimerCard()
        }
        
        // 액션 버튼들
        item {
            ActionButtons(
                onShare = onShare,
                onSave = onSave,
                onRetry = onRetry
            )
        }
    }
}

@Composable
private fun ResultSummaryCard(report: IntoxicationReport) {
    val (backgroundColor, textColor) = when (report.level) {
        IntoxicationLevel.NORMAL -> Color(0xFFE8F5E8) to Color(0xFF2E7D32)
        IntoxicationLevel.SLIGHTLY -> Color(0xFFFFF4E5) to Color(0xFFE65100)
        IntoxicationLevel.MODERATE -> Color(0xFFFFE0B2) to Color(0xFFBF360C)
        IntoxicationLevel.HEAVY -> Color(0xFFFDEBEC) to Color(0xFFC62828)
    }
    
    val levelText = when (report.level) {
        IntoxicationLevel.NORMAL -> "정상"
        IntoxicationLevel.SLIGHTLY -> "조금 취함"
        IntoxicationLevel.MODERATE -> "적당히 취함"
        IntoxicationLevel.HEAVY -> "과음"
    }
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = backgroundColor)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = levelText,
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
                color = textColor
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = "신뢰도: ${(report.confidence * 100).toInt()}%",
                style = MaterialTheme.typography.bodyLarge,
                color = textColor
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Text(
                text = report.summary,
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
                color = textColor
            )
        }
    }
}

@Composable
private fun SensorDataCard(sensorData: IntegratedSensorData) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "📊 센서 데이터",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // 얼굴 인식 데이터
            SensorDataRow(
                title = "👤 얼굴 인식",
                items = listOf(
                    "신뢰도: ${(sensorData.faceAnalysis.confidence * 100).toInt()}%",
                    "눈 상태: ${if (sensorData.faceAnalysis.eyesClosed) "감김" else "뜸"}",
                    "입 상태: ${if (sensorData.faceAnalysis.mouthOpen) "벌림" else "다물음"}",
                    "기울기: ${sensorData.faceAnalysis.faceAngle.toInt()}°"
                )
            )
            
            Divider(modifier = Modifier.padding(vertical = 8.dp))
            
            // 심박수 데이터
            SensorDataRow(
                title = "❤️ 심박수",
                items = listOf(
                    "심박수: ${sensorData.heartRate.bpm} BPM",
                    "변이도: ${String.format("%.2f", sensorData.heartRate.variability)}",
                    "측정시간: ${sensorData.heartRate.measurementDuration}초"
                )
            )
            
            Divider(modifier = Modifier.padding(vertical = 8.dp))
            
            // 자이로센서 데이터
            SensorDataRow(
                title = "📱 움직임 센서",
                items = listOf(
                    "흔들림: ${(sensorData.gyroscope.shakingIntensity * 100).toInt()}%",
                    "안정성: ${(sensorData.gyroscope.stabilityScore * 100).toInt()}%",
                    "최대움직임: ${String.format("%.2f", sensorData.gyroscope.peakMovement)}"
                )
            )
        }
    }
}

@Composable
private fun SensorDataRow(title: String, items: List<String>) {
    Column {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold
        )
        Spacer(modifier = Modifier.height(4.dp))
        items.forEach { item ->
            Text(
                text = "• $item",
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(start = 8.dp, bottom = 2.dp)
            )
        }
    }
}

@Composable
private fun AnalysisReportCard(report: IntoxicationReport) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "🤖 AI 분석 보고서",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Text(
                text = report.detailedAnalysis,
                style = MaterialTheme.typography.bodyMedium,
                lineHeight = 20.sp
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = "분석 시간: ${report.timestamp.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun RecommendationsCard(recommendations: List<String>) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "💡 권장사항",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            recommendations.forEachIndexed { index, recommendation ->
                Row(
                    modifier = Modifier.padding(bottom = 8.dp)
                ) {
                    Text(
                        text = "${index + 1}. ",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = recommendation,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

@Composable
private fun DisclaimerCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF3E0))
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "⚠️ 면책 고지",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = Color(0xFFE65100)
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = "본 분석 결과는 의료 목적이 아니며, 운전 가능 여부를 판단하는 기준으로 사용할 수 없습니다. 음주 후에는 반드시 대중교통을 이용하시기 바랍니다.",
                style = MaterialTheme.typography.bodySmall,
                color = Color(0xFFBF360C),
                lineHeight = 16.sp
            )
        }
    }
}

@Composable
private fun ActionButtons(
    onShare: () -> Unit,
    onSave: () -> Unit,
    onRetry: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        OutlinedButton(
            onClick = onShare,
            modifier = Modifier.weight(1f)
        ) {
            Icon(Icons.Default.Share, contentDescription = null)
            Spacer(modifier = Modifier.width(4.dp))
            Text("공유")
        }
        
        FilledTonalButton(
            onClick = onSave,
            modifier = Modifier.weight(1f)
        ) {
            Text("💾")
            Spacer(modifier = Modifier.width(4.dp))
            Text("결과 기록")
        }
        
        Button(
            onClick = onRetry,
            modifier = Modifier.weight(1f)
        ) {
            Icon(Icons.Default.Refresh, contentDescription = null)
            Spacer(modifier = Modifier.width(4.dp))
            Text("다시")
        }
    }
}
