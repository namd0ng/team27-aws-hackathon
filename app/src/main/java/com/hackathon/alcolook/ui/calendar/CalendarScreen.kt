package com.hackathon.alcolook.ui.calendar

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.layout.layout
import androidx.compose.ui.unit.offset
import com.hackathon.alcolook.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalendarScreen() {
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("월별", "통계")
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AppBackground)
    ) {
        // Header with title
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(CardBackground)
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            Text(
                text = "캘린더",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.SemiBold,
                color = TextPrimary
            )
        }
        
        // Tab Row
        TabRow(
            selectedTabIndex = selectedTab,
            modifier = Modifier.fillMaxWidth(),
            containerColor = TabBackground,
            contentColor = TabSelected,
            indicator = { tabPositions ->
                TabRowDefaults.Indicator(
                    color = TabIndicator,
                    height = 2.dp
                )
            },
            divider = {}
        ) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTab == index,
                    onClick = { selectedTab = index },
                    modifier = Modifier.height(48.dp)
                ) {
                    Text(
                        text = title,
                        color = if (selectedTab == index) TabSelected else TabUnselected,
                        fontWeight = if (selectedTab == index) FontWeight.SemiBold else FontWeight.Normal,
                        fontSize = 16.sp
                    )
                }
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
            .background(AppBackground)
    ) {
        // Month Header Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            colors = CardDefaults.cardColors(containerColor = CardBackground),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
            shape = RoundedCornerShape(12.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "2025년 1월",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = TextPrimary
                )
                TextButton(
                    onClick = { /* TODO: Date picker */ },
                    colors = ButtonDefaults.textButtonColors(contentColor = TabSelected)
                ) {
                    Text(
                        text = "날짜 이동",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
        
        // Calendar Grid Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            colors = CardDefaults.cardColors(containerColor = CardBackground),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                // Days of week header
                val daysOfWeek = listOf("일", "월", "화", "수", "목", "금", "토")
                Row(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    daysOfWeek.forEach { day ->
                        Box(
                            modifier = Modifier.weight(1f),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = day,
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Medium,
                                color = TextSecondary,
                                modifier = Modifier.padding(vertical = 8.dp)
                            )
                        }
                    }
                }
                
                Divider(
                    color = DividerColor,
                    thickness = 0.5.dp
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // Calendar days grid
                val daysInMonth = (1..31).toList()
                val startPadding = 3 // January 1st starts on Wednesday
                val totalCells = startPadding + daysInMonth.size
                val rows = (totalCells + 6) / 7
                
                repeat(rows) { row ->
                    Row(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        repeat(7) { col ->
                            val cellIndex = row * 7 + col
                            val dayNumber = cellIndex - startPadding + 1
                            
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .aspectRatio(1f)
                                    .padding(2.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                if (dayNumber in 1..31) {
                                    CalendarDayCell(
                                        day = dayNumber,
                                        isToday = dayNumber == 15,
                                        status = when {
                                            dayNumber % 7 == 0 -> DrinkingStatus.DANGER
                                            dayNumber % 5 == 0 -> DrinkingStatus.WARNING
                                            else -> DrinkingStatus.NORMAL
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Selected Date Records Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            colors = CardDefaults.cardColors(containerColor = CardBackground),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "1월 15일 기록",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = TextPrimary
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "아직 기록이 없어요",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary
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
            .background(AppBackground)
            .padding(16.dp)
    ) {
        // Period Toggle
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = CardBackground),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
            shape = RoundedCornerShape(12.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.Center
            ) {
                periods.forEachIndexed { index, period ->
                    FilterChip(
                        onClick = { selectedPeriod = index },
                        label = { 
                            Text(
                                text = period,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium
                            )
                        },
                        selected = selectedPeriod == index,
                        modifier = Modifier.padding(horizontal = 4.dp),
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = TabSelected.copy(alpha = 0.1f),
                            selectedLabelColor = TabSelected
                        )
                    )
                }
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Character Comment Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = CalendarSelected),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "🐕",
                    fontSize = 48.sp
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "이번 주는 건강한 한 주였어요!",
                    style = MaterialTheme.typography.bodyLarge,
                    textAlign = TextAlign.Center,
                    fontWeight = FontWeight.Medium,
                    color = TextPrimary
                )
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Health Index Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = CardBackground),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "건강 지수",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = TextPrimary
                )
                Spacer(modifier = Modifier.height(16.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    HealthStatusBadge("양호", 5, StatusNormal)
                    HealthStatusBadge("주의", 1, StatusWarning)
                    HealthStatusBadge("폭음", 0, StatusDanger)
                }
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Weekly Trend Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = CardBackground),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "주별 트렌드",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = TextPrimary
                )
                Spacer(modifier = Modifier.height(16.dp))
                
                // Simple bar chart
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.Bottom
                ) {
                    repeat(7) { index ->
                        val height = (20 + index * 8).dp
                        Box(
                            modifier = Modifier
                                .width(20.dp)
                                .height(height)
                                .background(
                                    TabSelected.copy(alpha = 0.8f),
                                    RoundedCornerShape(2.dp)
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
    isToday: Boolean = false,
    status: DrinkingStatus
) {
    val backgroundColor = when {
        isToday -> CalendarToday
        status == DrinkingStatus.WARNING -> WarningSoft
        status == DrinkingStatus.DANGER -> DangerSoft
        else -> Color.Transparent
    }
    
    val textColor = when {
        isToday -> CardBackground
        else -> TextPrimary
    }
    
    Box(
        modifier = Modifier
            .size(32.dp)
            .clip(CircleShape)
            .background(backgroundColor)
            .clickable { /* TODO: Select date */ },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = day.toString(),
            style = MaterialTheme.typography.bodyMedium,
            color = textColor,
            fontWeight = if (isToday) FontWeight.SemiBold else FontWeight.Normal
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
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = TextSecondary,
            fontSize = 12.sp
        )
        Text(
            text = "${count}일",
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.SemiBold,
            color = TextPrimary,
            fontSize = 12.sp
        )
    }
}

enum class DrinkingStatus {
    NORMAL, WARNING, DANGER
}