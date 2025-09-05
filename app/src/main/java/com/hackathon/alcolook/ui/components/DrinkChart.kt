package com.hackathon.alcolook.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hackathon.alcolook.data.model.DrinkingStatus
import com.hackathon.alcolook.ui.theme.*

@Composable
fun DrinkChart(
    data: List<ChartData>,
    modifier: Modifier = Modifier,
    isWeekly: Boolean = true
) {
    if (data.isEmpty()) {
        Box(
            modifier = modifier.height(150.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "데이터가 없습니다",
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary
            )
        }
        return
    }

    val maxValue = data.maxOfOrNull { it.value } ?: 1f
    val adjustedMaxValue = if (maxValue < 2f) 2f else maxValue * 1.2f

    Column(modifier = modifier) {
        // 차트 영역
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(120.dp)
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.Bottom
        ) {
            data.forEach { chartData ->
                Column(
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // 값 표시
                    if (chartData.value > 0) {
                        Text(
                            text = String.format("%.1f", chartData.value),
                            style = MaterialTheme.typography.bodySmall,
                            fontSize = 10.sp,
                            color = TextPrimary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                    } else {
                        Spacer(modifier = Modifier.height(18.dp))
                    }
                    
                    // 막대
                    val barHeight = if (chartData.value > 0) {
                        ((chartData.value / adjustedMaxValue) * 100).coerceAtLeast(4f).dp
                    } else {
                        4.dp
                    }
                    
                    Box(
                        modifier = Modifier
                            .width(24.dp)
                            .height(barHeight)
                            .clip(RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp))
                            .background(if (chartData.value > 0) chartData.color else AppBackground)
                    )
                }
            }
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        // X축 라벨
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            data.forEach { chartData ->
                Text(
                    text = chartData.label,
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

data class ChartData(
    val label: String,
    val value: Float,
    val color: Color,
    val status: DrinkingStatus = DrinkingStatus.APPROPRIATE
)