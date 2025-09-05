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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnalysisResultScreen(
    drunkLevel: Float,
    onBack: () -> Unit
) {
    // 테스트 데이터 생성
    val faceScore = (drunkLevel * 0.8f).toInt()
    val heartRate = (70 + drunkLevel * 0.5f).toInt()
    val gyroScore = (100 - drunkLevel * 0.6f).toInt()
    val totalScore = ((faceScore + heartRate + gyroScore) / 3).toInt()
    
    val level = when (totalScore) {
        in 80..100 -> "정상"
        in 60..79 -> "조금 취함"
        in 40..59 -> "적당히 취함"
        else -> "과음"
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
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            // 뒤로가기 버튼
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(onClick = onBack) {
                    Text("← 뒤로가기")
                }
                Spacer(modifier = Modifier.weight(1f))
                Text(
                    text = "분석 결과",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.weight(1f))
            }
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
                        text = level,
                        style = MaterialTheme.typography.headlineLarge,
                        fontWeight = FontWeight.Bold,
                        color = textColor
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Text(
                        text = "종합 점수: ${totalScore}점",
                        style = MaterialTheme.typography.titleLarge,
                        color = textColor
                    )
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    Text(
                        text = "3가지 센서 데이터를 종합하여 분석한 결과입니다.",
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Center,
                        color = textColor
                    )
                }
            }
        }
        
        item {
            // 센서 데이터 상세
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "📊 센서 데이터 분석",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // 얼굴 인식
                    SensorDataRow(
                        title = "👤 얼굴 인식",
                        score = faceScore,
                        details = listOf(
                            "음주 확률: ${drunkLevel.toInt()}%",
                            "눈 상태: ${if (drunkLevel > 50) "감김" else "정상"}",
                            "입 상태: ${if (drunkLevel > 40) "벌림" else "정상"}"
                        )
                    )
                    
                    Divider(modifier = Modifier.padding(vertical = 12.dp))
                    
                    // 심박수
                    SensorDataRow(
                        title = "❤️ 심박수",
                        score = heartRate,
                        details = listOf(
                            "심박수: ${heartRate} BPM",
                            "상태: ${if (heartRate in 60..100) "정상" else "주의"}",
                            "변이도: 정상 범위"
                        )
                    )
                    
                    Divider(modifier = Modifier.padding(vertical = 12.dp))
                    
                    // 자이로센서
                    SensorDataRow(
                        title = "📱 움직임 센서",
                        score = gyroScore,
                        details = listOf(
                            "안정성: ${gyroScore}%",
                            "흔들림: ${if (gyroScore < 70) "높음" else "낮음"}",
                            "균형감: ${if (gyroScore > 80) "양호" else "주의"}"
                        )
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
                        style = MaterialTheme.typography.titleLarge,
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
        
        item {
            // 면책 고지
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
        
        item {
            // 액션 버튼들
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = { /* TODO: 공유 기능 */ },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("📤 공유")
                }
                
                Button(
                    onClick = { /* TODO: 캘린더 저장 */ },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("💾 기록 저장")
                }
            }
        }
    }
}

@Composable
private fun SensorDataRow(
    title: String,
    score: Int,
    details: List<String>
) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = "${score}점",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = when {
                    score >= 80 -> Color(0xFF2E7D32)
                    score >= 60 -> Color(0xFFE65100)
                    else -> Color(0xFFC62828)
                }
            )
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        details.forEach { detail ->
            Text(
                text = "• $detail",
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(start = 8.dp, bottom = 4.dp)
            )
        }
    }
}
