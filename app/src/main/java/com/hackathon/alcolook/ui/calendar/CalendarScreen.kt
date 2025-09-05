package com.hackathon.alcolook.ui.calendar

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
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
import com.hackathon.alcolook.ui.theme.*

enum class DrinkingStatus {
    NORMAL, WARNING, DANGER
}

@Composable
fun CalendarDayCell(
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
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(CardBackground)
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            Text(
                text = "음주 기록",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.SemiBold,
                color = TextPrimary
            )
            
            Button(
                onClick = { /* TODO: Add record */ },
                modifier = Modifier.align(Alignment.CenterEnd),
                colors = ButtonDefaults.buttonColors(
                    containerColor = TabSelected
                ),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    text = "기록 추가",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = CardBackground
                )
            }
        }
        
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
                IconButton(
                    onClick = { /* TODO: Previous month */ }
                ) {
                    Text(
                        text = "<",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = TabSelected
                    )
                }
                
                TextButton(
                    onClick = { /* TODO: Date picker */ },
                    colors = ButtonDefaults.textButtonColors(contentColor = TextPrimary)
                ) {
                    Text(
                        text = "2025년 9월",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.SemiBold
                    )
                }
                
                IconButton(
                    onClick = { /* TODO: Next month */ }
                ) {
                    Text(
                        text = ">",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = TabSelected
                    )
                }
            }
        }
        
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
                
                HorizontalDivider(
                    color = DividerColor,
                    thickness = 0.5.dp
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                val daysInMonth = (1..30).toList()
                val startPadding = 0
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
                                if (dayNumber in 1..30) {
                                    CalendarDayCell(
                                        day = dayNumber,
                                        isToday = dayNumber == 5,
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
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "9월 5일 기록",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = TextPrimary
                    )
                    
                    OutlinedButton(
                        onClick = { /* TODO: Show summary */ },
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = TabSelected
                        ),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = "요약 보기",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "🍺",
                        fontSize = 32.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "기록된 음주가 없습니다",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    Button(
                        onClick = { /* TODO: Add first record */ },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = TabSelected
                        ),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = "첫 기록 추가하기",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            color = CardBackground
                        )
                    }
                }
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
        Row(
            modifier = Modifier.fillMaxWidth(),
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
        
        Spacer(modifier = Modifier.height(16.dp))
        
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
                    text = "멍멍! 적당한 음주 패턴이에요. 건강한 음주 습관을 유지하고 계시네요! 👏",
                    style = MaterialTheme.typography.bodyLarge,
                    textAlign = TextAlign.Center,
                    fontWeight = FontWeight.Medium,
                    color = TextPrimary
                )
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = CardBackground),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "📊",
                        fontSize = 16.sp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "주간 건강 지수",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = TextPrimary
                    )
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "현재 상태",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary
                    )
                    Text(
                        text = "좋음",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = StatusNormal
                    )
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                LinearProgressIndicator(
                    progress = 0.3f,
                    modifier = Modifier.fillMaxWidth(),
                    color = StatusNormal,
                    trackColor = AppBackground
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Text(
                    text = "일평균 0.5잔 (3.5잔/7일)",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Card(
                modifier = Modifier.weight(1f),
                colors = CardDefaults.cardColors(containerColor = CardBackground),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "🍺",
                            fontSize = 16.sp
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "총 음주량",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "3.5",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Text(
                        text = "잔",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )
                }
            }
            
            Card(
                modifier = Modifier.weight(1f),
                colors = CardDefaults.cardColors(containerColor = CardBackground),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "🍶",
                            fontSize = 16.sp
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "선호 음주",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary
                        )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "소주",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = TextPrimary
                    )
                    Text(
                        text = "가장 많이",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )
                }
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = CardBackground),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "📸",
                        fontSize = 16.sp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "얼굴 분석 결과",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = TextPrimary
                    )
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(24.dp)
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "2",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Text(
                            text = "분석 횟수",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary
                        )
                    }
                    
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "79%",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Text(
                            text = "평균 확률",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun HealthStatusBadge(
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
}