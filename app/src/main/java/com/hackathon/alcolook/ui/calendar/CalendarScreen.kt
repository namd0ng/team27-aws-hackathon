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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.hackathon.alcolook.ui.theme.*
import com.hackathon.alcolook.data.model.DrinkType
import com.hackathon.alcolook.data.model.DrinkUnit
import com.hackathon.alcolook.data.model.DrinkingStatus
import com.hackathon.alcolook.ui.viewmodel.SimpleCalendarViewModel
import com.hackathon.alcolook.ui.components.AddRecordDialog
import com.hackathon.alcolook.ui.components.DrinkChart
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@Composable
fun CalendarDayCell(
    day: Int,
    isToday: Boolean = false,
    isSelected: Boolean = false,
    status: DrinkingStatus = DrinkingStatus.NORMAL,
    onClick: () -> Unit = {}
) {
    val backgroundColor = when {
        isToday -> CalendarToday
        isSelected -> CalendarSelected
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
fun CalendarScreen(
    viewModel: SimpleCalendarViewModel = viewModel()
) {
    val selectedTab by viewModel.selectedTab.collectAsStateWithLifecycle()
    val tabs = listOf("📅 월별", "📊 통계")
    var showAddDialog by remember { mutableStateOf(false) }
    
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
            
            Row(
                modifier = Modifier.align(Alignment.CenterEnd),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = { viewModel.selectTab(0) },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = androidx.compose.ui.graphics.Color.Green
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = "캘린더",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = CardBackground
                    )
                }
                
                Button(
                    onClick = { viewModel.selectTab(1) },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = androidx.compose.ui.graphics.Color.Blue
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = "통계",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = CardBackground
                    )
                }
                
                Button(
                    onClick = { 
                        android.util.Log.d("AlcoLook", "기록 추가 버튼 클릭됨")
                        showAddDialog = true 
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = androidx.compose.ui.graphics.Color.Red
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
                    onClick = { viewModel.selectTab(index) },
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
            0 -> MonthlyCalendarContent(viewModel) { showAddDialog = true }
            1 -> StatisticsContent(viewModel)
        }
        
        if (showAddDialog) {
            println("DEBUG: AddRecordDialog 표시 중")
            AddRecordDialog(
                onDismiss = { showAddDialog = false },
                onConfirm = { type, unit, quantity, abv, drinkName, note ->
                    viewModel.addDrinkRecord(
                        type = type,
                        unit = unit, 
                        quantity = quantity,
                        customAbv = abv,
                        customDrinkName = drinkName,
                        note = note
                    )
                    showAddDialog = false
                }
            )
        }
    }
}

@Composable
private fun MonthlyCalendarContent(
    viewModel: SimpleCalendarViewModel,
    onAddRecord: () -> Unit
) {
    val selectedDate by viewModel.selectedDate.collectAsStateWithLifecycle()
    val records by viewModel.records.collectAsStateWithLifecycle()
    val today = LocalDate.now()
    val currentMonth = java.time.YearMonth.now()
    
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
                    onClick = { /* TODO */ }
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
                        text = currentMonth.format(DateTimeFormatter.ofPattern("yyyy년 M월")),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.SemiBold
                    )
                }
                
                IconButton(
                    onClick = { /* TODO */ }
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
                                    val hasRecord = records.any { it.date == date }
                                    val isToday = date == today
                                    val isSelected = date == selectedDate
                                    
                                    CalendarDayCell(
                                        day = dayNumber,
                                        isToday = isToday,
                                        isSelected = isSelected,
                                        status = if (hasRecord) DrinkingStatus.WARNING else DrinkingStatus.NORMAL,
                                        onClick = { viewModel.selectDate(date) }
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
                        text = "${selectedDate.format(DateTimeFormatter.ofPattern("M월 d일"))} 기록",
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
                
                val selectedDateRecords = records.filter { it.date == selectedDate }
                if (selectedDateRecords.isEmpty()) {
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
                            onClick = { 
                                println("DEBUG: 첫 기록 추가 버튼 클릭됨")
                                onAddRecord() 
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = androidx.compose.ui.graphics.Color.Green
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
                } else {
                    selectedDateRecords.forEach { record ->
                        Text(
                            text = "${record.type.getDisplayName()} ${record.quantity}${record.unit.getDisplayName()}",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun StatisticsContent(
    viewModel: SimpleCalendarViewModel
) {
    val selectedPeriod by viewModel.selectedPeriod.collectAsStateWithLifecycle()
    val weeklyStats by viewModel.weeklyStats.collectAsStateWithLifecycle()
    val monthlyStats by viewModel.monthlyStats.collectAsStateWithLifecycle()
    val healthStatus by viewModel.healthStatus.collectAsStateWithLifecycle()
    val weeklyChartData by viewModel.weeklyChartData.collectAsStateWithLifecycle()
    val monthlyChartData by viewModel.monthlyChartData.collectAsStateWithLifecycle()
    val periods = listOf("주간 요약", "월간 요약")
    
    val currentStats = if (selectedPeriod == 0) weeklyStats else monthlyStats
    val currentChartData = if (selectedPeriod == 0) weeklyChartData else monthlyChartData
    val hasData = if (selectedPeriod == 0) weeklyStats.totalStandardDrinks > 0 else monthlyStats.totalStandardDrinks > 0
    
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
                    onClick = { viewModel.selectPeriod(index) },
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
        
        if (!hasData) {
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
                        text = "아직 기록이 없어요! 음주 기록을 추가해보세요.",
                        style = MaterialTheme.typography.bodyLarge,
                        textAlign = TextAlign.Center,
                        fontWeight = FontWeight.Medium,
                        color = TextPrimary
                    )
                }
            }
        } else {
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
                        text = viewModel.getCharacterComment(healthStatus),
                        style = MaterialTheme.typography.bodyLarge,
                        textAlign = TextAlign.Center,
                        fontWeight = FontWeight.Medium,
                        color = TextPrimary
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
                        text = "📊",
                        fontSize = 16.sp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "건강 지수",
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
                        text = when(healthStatus) {
                            DrinkingStatus.NORMAL -> "양호"
                            DrinkingStatus.WARNING -> "주의"
                            DrinkingStatus.DANGER -> "위험"
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = when(healthStatus) {
                            DrinkingStatus.NORMAL -> StatusNormal
                            DrinkingStatus.WARNING -> StatusWarning
                            DrinkingStatus.DANGER -> StatusDanger
                        }
                    )
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                LinearProgressIndicator(
                    progress = if (selectedPeriod == 0) {
                        (weeklyStats.totalStandardDrinks / 14f).coerceAtMost(1f)
                    } else {
                        (monthlyStats.totalStandardDrinks / 60f).coerceAtMost(1f)
                    },
                    modifier = Modifier.fillMaxWidth(),
                    color = when(healthStatus) {
                        DrinkingStatus.NORMAL -> StatusNormal
                        DrinkingStatus.WARNING -> StatusWarning
                        DrinkingStatus.DANGER -> StatusDanger
                    },
                    trackColor = AppBackground
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Text(
                    text = if (selectedPeriod == 0) {
                        "일평균 ${String.format("%.1f", weeklyStats.averagePerDay)}잔 (총 ${String.format("%.1f", weeklyStats.totalStandardDrinks)}잔)"
                    } else {
                        "일평균 ${String.format("%.1f", monthlyStats.averagePerDay)}잔 (총 ${String.format("%.1f", monthlyStats.totalStandardDrinks)}잔)"
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        if (hasData) {
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
                            text = "📈",
                            fontSize = 16.sp
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (selectedPeriod == 0) "주간 트렌드" else "월간 트렌드",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = TextPrimary
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    DrinkChart(
                        data = currentChartData,
                        isWeekly = selectedPeriod == 0,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
        }
        
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
                        text = if (selectedPeriod == 0) {
                            String.format("%.1f", weeklyStats.totalStandardDrinks)
                        } else {
                            String.format("%.1f", monthlyStats.totalStandardDrinks)
                        },
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
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = if (selectedPeriod == 0 && weeklyStats.totalStandardDrinks > 0) {
                            weeklyStats.favoriteType.getDisplayName()
                        } else {
                            "-"
                        },
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
    }
}