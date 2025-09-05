package com.hackathon.alcolook.ui.calendar

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hackathon.alcolook.ui.theme.WarningSoft
import com.hackathon.alcolook.ui.theme.DangerSoft

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalendarScreen() {
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("월별", "통계")
    
    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        // Header
        Text(
            text = "캘린더",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(16.dp)
        )
        
        // Tab Row
        TabRow(
            selectedTabIndex = selectedTab,
            modifier = Modifier.fillMaxWidth()
        ) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTab == index,
                    onClick = { selectedTab = index },
                    text = { Text(title) }
                )
            }
        }
        
        // Tab Content
        when (selectedTab) {
            0 -> MonthlyCalendarContent()
            1 -> StatisticsContent()
        }
    }
}

@Composable
private fun MonthlyCalendarContent() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Month Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "2025년 1월",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            TextButton(onClick = { /* TODO: Date picker */ }) {
                Text("날짜 이동")
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Calendar Grid
        val daysOfWeek = listOf("일", "월", "화", "수", "목", "금", "토")
        val daysInMonth = (1..31).toList()
        
        // Days of week header
        LazyVerticalGrid(
            columns = GridCells.Fixed(7),
            modifier = Modifier.fillMaxWidth()
        ) {
            items(daysOfWeek) { day ->
                Text(
                    text = day,
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(8.dp)
                )
            }
            
            items(daysInMonth) { day ->
                CalendarDayCell(
                    day = day,
                    status = when {
                        day % 7 == 0 -> DrinkingStatus.DANGER // 폭음
                        day % 5 == 0 -> DrinkingStatus.WARNING // 주의
                        else -> DrinkingStatus.NORMAL // 양호
                    }
                )
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Selected Date Records
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "1월 15일 기록",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "아직 기록이 없어요",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun StatisticsContent() {
    var selectedPeriod by remember { mutableIntStateOf(0) }
    val periods = listOf("주간 요약", "월간 요약")
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Period Toggle
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center
        ) {
            periods.forEachIndexed { index, period ->
                FilterChip(
                    onClick = { selectedPeriod = index },
                    label = { Text(period) },
                    selected = selectedPeriod == index,
                    modifier = Modifier.padding(horizontal = 4.dp)
                )
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Character Comment Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer
            )
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "🐕",
                    fontSize = 48.sp
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "이번 주는 건강한 한 주였어요!",
                    style = MaterialTheme.typography.bodyLarge,
                    textAlign = TextAlign.Center,
                    fontWeight = FontWeight.Medium
                )
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Health Index
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "건강 지수",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(12.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    HealthStatusBadge("양호", 5, Color(0xFF4CAF50))
                    HealthStatusBadge("주의", 1, Color(0xFFFF9800))
                    HealthStatusBadge("폭음", 0, Color(0xFFF44336))
                }
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Weekly Trend (Simple Bar Chart)
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "주별 트렌드",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(12.dp))
                
                // Simple bar chart placeholder
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.Bottom
                ) {
                    repeat(7) { index ->
                        val height = (20 + index * 10).dp
                        Box(
                            modifier = Modifier
                                .width(24.dp)
                                .height(height)
                                .background(
                                    MaterialTheme.colorScheme.primary,
                                    RoundedCornerShape(4.dp)
                                )
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CalendarDayCell(
    day: Int,
    status: DrinkingStatus
) {
    val backgroundColor = when (status) {
        DrinkingStatus.WARNING -> WarningSoft
        DrinkingStatus.DANGER -> DangerSoft
        else -> Color.Transparent
    }
    
    Box(
        modifier = Modifier
            .size(40.dp)
            .clip(CircleShape)
            .background(backgroundColor),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = day.toString(),
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun HealthStatusBadge(
    label: String,
    count: Int,
    color: Color
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(12.dp)
                .clip(CircleShape)
                .background(color)
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall
        )
        Text(
            text = "${count}일",
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Bold
        )
    }
}

enum class DrinkingStatus {
    NORMAL, WARNING, DANGER
}