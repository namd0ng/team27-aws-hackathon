package com.hackathon.alcolook.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hackathon.alcolook.data.HeartRateData
import com.hackathon.alcolook.data.GyroscopeData

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ComprehensiveAnalysisScreen(
    faceAnalysisResult: Float?,
    heartRateData: HeartRateData?,
    gyroscopeData: GyroscopeData?,
    onBackClick: () -> Unit,
    onSaveClick: () -> Unit
) {
    // 종합 점수 계산
    val totalScore = calculateTotalScore(faceAnalysisResult, heartRateData, gyroscopeData)
    val status = when (totalScore) {
        in 80..100 -> "정상"
        in 60..79 -> "주의"
        in 40..59 -> "위험"
        else -> "매우위험"
    }
    
    val backgroundColor = when (totalScore) {
        in 80..100 -> Color(0xFFE8F5E8)
        in 60..79 -> Color(0xFFFFF4E5)
        in 40..59 -> Color(0xFFFFE0B2)
        else -> Color(0xFFFDEBEC)
    }
    
    val textColor = when (totalScore) {
        in 80..100 -> Color(0xFF2E7D32)
        in 60..79 -> Color(0xFFE65100)
        in 40..59 -> Color(0xFFBF360C)
        else -> Color(0xFFC62828)
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            // 제목
            Text(
                text = "📊 종합 분석 결과",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        }
        
        item {
            // 최종 결과 카드
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
                        text = status,
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold,
                        color = textColor
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Text(
                        text = "종합 점수: ${totalScore}점",
                        fontSize = 18.sp,
                        color = textColor
                    )
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    Text(
                        text = "3가지 센서 데이터를 종합하여 분석한 결과입니다.",
                        fontSize = 14.sp,
                        textAlign = TextAlign.Center,
                        color = textColor
                    )
                }
            }
        }
        
        item {
            // 측정 데이터 요약
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "📋 측정 데이터 요약",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    // 얼굴 분석
                    SensorDataRow(
                        title = "👤 얼굴 분석",
                        value = if (faceAnalysisResult != null) "${faceAnalysisResult.toInt()}%" else "측정 안함",
                        score = if (faceAnalysisResult != null) (100 - faceAnalysisResult).toInt() else null
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    // 심박수
                    SensorDataRow(
                        title = "❤️ 심박수",
                        value = if (heartRateData != null) "${heartRateData.bpm} BPM" else "측정 안함",
                        score = if (heartRateData != null) {
                            when {
                                heartRateData.bpm in 60..90 -> 100
                                heartRateData.bpm in 50..110 -> 80
                                else -> 50
                            }
                        } else null
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    // 균형감각
                    SensorDataRow(
                        title = "🚶 균형감각",
                        value = if (gyroscopeData != null) "점수 ${String.format("%.1f", gyroscopeData.stabilityScore)}" else "측정 안함",
                        score = if (gyroscopeData != null) (gyroscopeData.stabilityScore * 100).toInt() else null
                    )
                }
            }
        }
        
        item {
            // 권장사항
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "💡 권장사항",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    val recommendations = when (totalScore) {
                        in 80..100 -> listOf(
                            "현재 상태가 양호합니다",
                            "적당한 수분 섭취를 권장합니다",
                            "안전한 귀가를 위해 대중교통을 이용하세요"
                        )
                        in 60..79 -> listOf(
                            "주의가 필요한 상태입니다",
                            "충분한 휴식을 취하세요",
                            "물을 많이 마시고 운전은 피하세요"
                        )
                        in 40..59 -> listOf(
                            "위험한 상태입니다",
                            "즉시 음주를 중단하세요",
                            "안전한 장소에서 휴식하세요"
                        )
                        else -> listOf(
                            "매우 위험한 상태입니다",
                            "즉시 의료진의 도움을 받으세요",
                            "혼자 있지 말고 응급상황에 대비하세요"
                        )
                    }
                    
                    recommendations.forEachIndexed { index, recommendation ->
                        Row(
                            modifier = Modifier.padding(bottom = 8.dp)
                        ) {
                            Text(
                                text = "${index + 1}. ",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = recommendation,
                                fontSize = 14.sp,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }
        }
        
        item {
            // 면책 고지
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "⚠️ 중요 안내",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Text(
                        text = "본 분석 결과는 의료 목적이 아니며, 실제 운전 가능 여부 판단에 사용하지 마세요. 음주 후에는 반드시 대중교통을 이용하시기 바랍니다.",
                        fontSize = 14.sp,
                        lineHeight = 20.sp
                    )
                }
            }
        }
        
        item {
            // 버튼들
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = onBackClick,
                    modifier = Modifier.weight(1f)
                ) {
                    Text("다시 측정")
                }
                
                Button(
                    onClick = onSaveClick,
                    modifier = Modifier.weight(1f)
                ) {
                    Text("결과 저장")
                }
            }
        }
    }
}

@Composable
private fun SensorDataRow(
    title: String,
    value: String,
    score: Int?
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            fontSize = 14.sp
        )
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = value,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold
            )
            if (score != null) {
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "(${score}점)",
                    fontSize = 12.sp,
                    color = when {
                        score >= 80 -> Color(0xFF2E7D32)
                        score >= 60 -> Color(0xFFE65100)
                        else -> Color(0xFFC62828)
                    }
                )
            }
        }
    }
}

private fun calculateTotalScore(
    face: Float?,
    heart: HeartRateData?,
    gyro: GyroscopeData?
): Int {
    var totalScore = 0f
    var count = 0
    
    // 얼굴 분석 (음주 확률이 낮을수록 점수 높음)
    if (face != null) {
        totalScore += (100 - face).coerceIn(0f, 100f)
        count++
    }
    
    // 심박수 (정상 범위일수록 점수 높음)
    if (heart != null) {
        val heartScore = when {
            heart.bpm in 60..90 -> 100f
            heart.bpm in 50..110 -> 80f
            else -> 50f
        }
        totalScore += heartScore
        count++
    }
    
    // 균형감각 (안정성이 높을수록 점수 높음)
    if (gyro != null) {
        totalScore += (gyro.stabilityScore * 100).coerceIn(0f, 100f)
        count++
    }
    
    return if (count > 0) (totalScore / count).toInt() else 50
}
