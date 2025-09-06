package com.hackathon.alcolook.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SimpleResultScreen(
    drunkLevel: Float,
    onBackClick: () -> Unit,
    onSaveRecord: (Float) -> Unit,
    onRetakePhoto: () -> Unit
) {
    val (emoji, message, color) = when {
        drunkLevel < 20f -> Triple("😊", "정상 상태입니다", MaterialTheme.colorScheme.primary)
        drunkLevel < 40f -> Triple("😐", "약간 취한 상태", MaterialTheme.colorScheme.tertiary)
        drunkLevel < 60f -> Triple("😵", "취한 상태", MaterialTheme.colorScheme.error)
        else -> Triple("🥴", "매우 취한 상태", MaterialTheme.colorScheme.error)
    }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "분석 결과",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold
        )
        
        Spacer(modifier = Modifier.weight(1f))
        
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = color.copy(alpha = 0.1f)
            )
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = emoji,
                    fontSize = 64.sp
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Text(
                    text = "${drunkLevel.toInt()}%",
                    fontSize = 48.sp,
                    fontWeight = FontWeight.Bold,
                    color = color
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Text(
                    text = message,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Medium,
                    textAlign = TextAlign.Center
                )
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Text(
            text = "⚠️ 면책 고지\n이 결과는 참고용이며, 운전 판단에 사용하지 마세요.",
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.weight(1f))
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedButton(
                onClick = onRetakePhoto,
                modifier = Modifier.weight(1f)
            ) {
                Text("다시 촬영")
            }
            
            Button(
                onClick = { onSaveRecord(drunkLevel) },
                modifier = Modifier.weight(1f)
            ) {
                Text("기록 저장")
            }
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        TextButton(
            onClick = onBackClick,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("홈으로 돌아가기")
        }
    }
}
