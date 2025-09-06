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
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter

enum class DrinkingStatusSimple {
    NORMAL, WARNING, DANGER
}

@Composable
fun CalendarDayCellSimple(
    day: Int,
    isToday: Boolean = false,
    isSelected: Boolean = false,
    status: DrinkingStatusSimple,
    onClick: () -> Unit = {}
) {
    val backgroundColor = when {
        isToday -> CalendarToday
        isSelected -> CalendarSelected
        status == DrinkingStatusSimple.WARNING -> WarningSoft
        status == DrinkingStatusSimple.DANGER -> DangerSoft
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
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = day.toString(),
            style = MaterialTheme.typography.bodyMedium,
            color = textColor,
            fontWeight = if (isToday || isSelected) FontWeight.SemiBold else FontWeight.Normal
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalendarScreenSimple() {
    var selectedTab by remember { mutableIntStateOf(0) }
    var selectedDate by remember { mutableStateOf(LocalDate.now()) }
    var currentMonth by remember { mutableStateOf(YearMonth.now()) }
    var showAddDialog by remember { mutableStateOf(false) }
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
                onClick = { showAddDialog = true },
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
            0 -> MonthlyCalendarContentSimple(
                currentMonth = currentMonth,
                selectedDate = selectedDate,
                onMonthChange = { currentMonth = it },
                onDateSelect = { selectedDate = it }
            )
            1 -> StatisticsContentSimple()
        }
        
        if (showAddDialog) {
            com.hackathon.alcolook.ui.components.AddRecordDialog(
                onDismiss = { showAddDialog = false },
                onConfirm = { type, unit, quantity, abv, drinkName, note ->
                    // TODO: 실제 데이터 저장 처리
                    showAddDialog = false
                }
            )
        }
    }
}

@Composable
private fun MonthlyCalendarContentSimple(
    currentMonth: YearMonth,
    selectedDate: LocalDate,
    onMonthChange: (YearMonth) -> Unit,
    onDateSelect: (LocalDate) -> Unit
) {
    val today = LocalDate.now()
    
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
                    onClick = { onMonthChange(currentMonth.minusMonths(1)) }
                ) {
                    Text(
                        text = "<",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = TabSelected
                    )
                }
                
                Text(
                    text = currentMonth.format(DateTimeFormatter.ofPattern("yyyy년 M월")),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = TextPrimary
                )
                
                IconButton(
                    onClick = { onMonthChange(currentMonth.plusMonths(1)) }
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
                
                val daysInMonth = (1..currentMonth.lengthOfMonth()).toList()
                val firstDayOfWeek = currentMonth.atDay(1).dayOfWeek.value % 7
                val totalCells = firstDayOfWeek + daysInMonth.size
                val rows = (totalCells + 6) / 7
                
                repeat(rows) { row ->
                    Row(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        repeat(7) { col ->
                            val cellIndex = row * 7 + col
                            val dayNumber = cellIndex - firstDayOfWeek + 1
                            
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .aspectRatio(1f)
                                    .padding(2.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                if (dayNumber in 1..currentMonth.lengthOfMonth()) {
                                    val date = currentMonth.atDay(dayNumber)
                                    val status = when {
                                        dayNumber % 7 == 0 -> DrinkingStatusSimple.DANGER
                                        dayNumber % 5 == 0 -> DrinkingStatusSimple.WARNING
                                        else -> DrinkingStatusSimple.NORMAL
                                    }
                                    val isToday = date == today
                                    val isSelected = date == selectedDate
                                    
                                    CalendarDayCellSimple(
                                        day = dayNumber,
                                        isToday = isToday,
                                        isSelected = isSelected,
                                        status = status,
                                        onClick = { onDateSelect(date) }
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
                Text(
                    text = "${selectedDate.format(DateTimeFormatter.ofPattern("M월 d일"))} 기록",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = TextPrimary
                )
                
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
                }
            }
        }
    }
}

@Composable
private fun StatisticsContentSimple() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AppBackground)
            .padding(16.dp)
    ) {
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
                    text = "건강한 음주 습관을 유지하고 계시네요!",
                    style = MaterialTheme.typography.bodyLarge,
                    textAlign = TextAlign.Center,
                    fontWeight = FontWeight.Medium,
                    color = TextPrimary
                )
            }
        }
    }
}