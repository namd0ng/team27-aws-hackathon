package com.hackathon.alcolook.ui.calendar

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.ArrowDropDown
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
import androidx.compose.ui.window.Dialog
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
    status: DrinkingStatus = DrinkingStatus.APPROPRIATE,
    hasRecord: Boolean = false,
    onClick: () -> Unit = {}
) {
    val backgroundColor = when {
        isSelected -> Color(0xFFc6c6c2).copy(alpha = 0.3f)
        hasRecord -> when (status) {
            DrinkingStatus.APPROPRIATE -> Color(0xFFE8F5E8)
            DrinkingStatus.CAUTION -> Color(0xFFFFF4E5)
            DrinkingStatus.EXCESSIVE -> Color(0xFFFDEBEC)
            DrinkingStatus.DANGEROUS -> Color(0xFFFFE0E0)
        }
        else -> Color.Transparent
    }
    
    val textColor = TextPrimary
    
    Box(
        modifier = Modifier
            .size(40.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(backgroundColor)
            .then(
                if (isToday) {
                    Modifier.border(2.dp, Color(0xFFc6c6c2), RoundedCornerShape(8.dp))
                } else {
                    Modifier
                }
            )
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = day.toString(),
            style = MaterialTheme.typography.bodyMedium,
            color = textColor,
            fontWeight = if (isToday || isSelected) FontWeight.Bold else FontWeight.Normal
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalendarScreen(
    viewModel: SimpleCalendarViewModel = viewModel()
) {
    val selectedTab by viewModel.selectedTab.collectAsStateWithLifecycle()
    val tabs = listOf("월별", "통계")
    var showAddDialog by remember { mutableStateOf(false) }
    var showEditDialog by remember { mutableStateOf(false) }
    var editingRecord by remember { mutableStateOf<com.hackathon.alcolook.data.model.DrinkRecord?>(null) }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AppBackground)
    ) {
        // Top Header - 음주 기록 제목과 기록 추가 버튼
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White)
                .padding(16.dp)
        ) {
            Text(
                text = "음주 기록",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )
            
            Button(
                onClick = { 
                    android.util.Log.d("AlcoLook", "기록 추가 버튼 클릭됨")
                    showAddDialog = true 
                },
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .height(32.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.Black
                ),
                shape = RoundedCornerShape(16.dp),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp)
            ) {
                Text(
                    text = "+ 기록 추가",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color.White
                )
            }
        }
        
        // 구분선
        HorizontalDivider(
            color = DividerColor,
            thickness = 0.5.dp,
            modifier = Modifier.padding(horizontal = 16.dp)
        )
        
        // Main Tab Row - 월별/통계 탭 (Figma 디자인 기준으로 중앙 정렬, 적절한 간격)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(CardBackground)
                .padding(horizontal = 16.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            tabs.forEachIndexed { index, title ->
                FilterChip(
                    onClick = { 
                        android.util.Log.d("AlcoLook", "Tab clicked: $index, current: $selectedTab")
                        viewModel.selectTab(index) 
                    },
                    label = { 
                        Box(
                            modifier = Modifier.fillMaxWidth(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = title,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    },
                    selected = selectedTab == index,
                    modifier = Modifier.weight(1f),
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = TabSelected.copy(alpha = 0.12f),
                        selectedLabelColor = TabSelected,
                        containerColor = Color.Transparent,
                        labelColor = TabUnselected
                    )
                )
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
        
        if (showEditDialog && editingRecord != null) {
            com.hackathon.alcolook.ui.components.EditRecordDialog(
                record = editingRecord!!,
                onDismiss = { 
                    showEditDialog = false
                    editingRecord = null
                },
                onConfirm = { type: DrinkType, unit: DrinkUnit, quantity: Int, abv: Float?, note: String? ->
                    viewModel.updateDrinkRecord(
                        recordId = editingRecord!!.id,
                        type = type,
                        unit = unit,
                        quantity = quantity,
                        customAbv = abv,
                        note = note
                    )
                    showEditDialog = false
                    editingRecord = null
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
    val currentMonth by viewModel.currentMonth.collectAsStateWithLifecycle()
    val records by viewModel.records.collectAsStateWithLifecycle()
    val dailyStatusMap by viewModel.dailyStatusMap.collectAsStateWithLifecycle()
    val selectedDateRecords = records.filter { it.date == selectedDate }
    var showDetailDialog by remember { mutableStateOf(false) }
    var showDatePicker by remember { mutableStateOf(false) }
    val today = LocalDate.now()
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AppBackground)
            .verticalScroll(rememberScrollState())
    ) {
        // Calendar Navigation + Grid (통합)
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            colors = CardDefaults.cardColors(containerColor = CardBackground),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column {
                // Calendar Navigation
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = { viewModel.goToPreviousMonth() }
                    ) {
                        Text(
                            text = "<",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.Black
                        )
                    }
                    
                    TextButton(
                        onClick = { showDatePicker = true },
                        colors = ButtonDefaults.textButtonColors(contentColor = TextPrimary)
                    ) {
                        Text(
                            text = currentMonth.format(DateTimeFormatter.ofPattern("yyyy년 M월")),
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                    
                    IconButton(
                        onClick = { viewModel.goToNextMonth() }
                    ) {
                        Text(
                            text = ">",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.Black
                        )
                    }
                }
                
                // Calendar Grid
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
                                        status = dailyStatusMap[date] ?: DrinkingStatus.APPROPRIATE,
                                        hasRecord = hasRecord,
                                        onClick = { viewModel.selectDate(date) }
                                    )
                                }
                            }
                        }
                    }
                }
                }
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Daily Record Section
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
                        onClick = { 
                            if (selectedDateRecords.isNotEmpty()) {
                                showDetailDialog = true 
                            }
                        },
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
                val selectedDateStatus = dailyStatusMap[selectedDate] ?: DrinkingStatus.APPROPRIATE
                val totalAlcohol = selectedDateRecords.sumOf { it.getPureAlcoholGrams().toDouble() }.toFloat()
                val totalStandardDrinks = selectedDateRecords.sumOf { it.getStandardDrinks().toDouble() }.toFloat()
                
                // 요약 정보 표시
                if (selectedDateRecords.isNotEmpty()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "총 음주량: ${String.format("%.1f", totalAlcohol)}g",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary
                        )
                        Text(
                            text = "표준잔수: ${String.format("%.1f", totalStandardDrinks)}잔",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary
                        )
                        Text(
                            text = when(selectedDateStatus) {
                                DrinkingStatus.APPROPRIATE -> "적정"
                                DrinkingStatus.CAUTION -> "주의"
                                DrinkingStatus.EXCESSIVE -> "과음"
                                DrinkingStatus.DANGEROUS -> "위험"
                            },
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.SemiBold,
                            color = when(selectedDateStatus) {
                                DrinkingStatus.APPROPRIATE -> androidx.compose.ui.graphics.Color.Green
                                DrinkingStatus.CAUTION -> androidx.compose.ui.graphics.Color(0xFFFF9800)
                                DrinkingStatus.EXCESSIVE -> androidx.compose.ui.graphics.Color.Red
                                DrinkingStatus.DANGEROUS -> androidx.compose.ui.graphics.Color.Black
                            }
                        )
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                }
                
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
                                containerColor = MaterialTheme.colorScheme.primary
                            ),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = "첫 기록 추가하기",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium,
                                color = Color.White
                            )
                        }
                    }
                } else {
                    // Show drink records with enhanced layout
                    selectedDateRecords.forEach { record ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "🍺",
                                fontSize = 24.sp,
                                modifier = Modifier.padding(end = 12.dp)
                            )
                            
                            Column(
                                modifier = Modifier.weight(1f)
                            ) {
                                Row(
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(
                                        text = "${record.type.getDisplayName()} ${record.quantity}${record.unit.getDisplayName()}",
                                        style = MaterialTheme.typography.bodyLarge,
                                        fontWeight = FontWeight.Medium,
                                        color = TextPrimary
                                    )
                                    Text(
                                        text = "${String.format("%.1f", record.getActualAbv())}%",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = TextSecondary
                                    )
                                }
                                
                                Text(
                                    text = "순수 알코올 ${String.format("%.1f", record.getPureAlcoholGrams())}g (표준잔 ${record.getFormattedStandardDrinks()}잔)",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = TextSecondary
                                )
                                
                                if (record.note?.isNotBlank() == true) {
                                    Text(
                                        text = "📝 ${record.note}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = TextSecondary
                                    )
                                }
                            }
                            
                            Row {
                                IconButton(
                                    onClick = { /* Edit record */ }
                                ) {
                                    Icon(
                                        Icons.Default.Edit,
                                        contentDescription = "수정",
                                        tint = TextSecondary
                                    )
                                }
                                IconButton(
                                    onClick = { 
                                        viewModel.deleteDrinkRecord(record.id)
                                    }
                                ) {
                                    Icon(
                                        Icons.Default.Delete,
                                        contentDescription = "삭제",
                                        tint = Color.Red
                                    )
                                }
                            }
                        }
                        
                        if (record != selectedDateRecords.last()) {
                            HorizontalDivider(
                                color = DividerColor.copy(alpha = 0.3f),
                                thickness = 0.5.dp
                            )
                        }
                    }
                }
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        if (showDatePicker) {
            DatePickerDialog(
                onDismiss = { showDatePicker = false },
                onDateSelected = { year, month, day ->
                    val newDate = LocalDate.of(year, month, day)
                    viewModel.selectDate(newDate)
                    showDatePicker = false
                }
            )
        }
        
        if (showDetailDialog) {
            val selectedDateRecords = records.filter { it.date == selectedDate }
            val dailyStatusMap by viewModel.dailyStatusMap.collectAsStateWithLifecycle()
            val selectedDateStatus = dailyStatusMap[selectedDate] ?: DrinkingStatus.APPROPRIATE
            val totalAlcohol = selectedDateRecords.sumOf { it.getPureAlcoholGrams().toDouble() }.toFloat()
            val totalStandardDrinks = selectedDateRecords.sumOf { it.getStandardDrinks().toDouble() }.toFloat()
            
            com.hackathon.alcolook.ui.components.RecordDetailDialog(
                records = selectedDateRecords,
                selectedDate = selectedDate,
                dailyStatus = selectedDateStatus,
                totalAlcohol = totalAlcohol,
                totalStandardDrinks = totalStandardDrinks,
                onDismiss = { showDetailDialog = false },
                onEditRecord = null,
                onDeleteRecord = { record ->
                    viewModel.deleteDrinkRecord(record.id)
                }
            )
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
    val weeklyDrinkTypeStats by viewModel.weeklyDrinkTypeStats.collectAsStateWithLifecycle()
    val monthlyDrinkTypeStats by viewModel.monthlyDrinkTypeStats.collectAsStateWithLifecycle()
    val periods = listOf("주간 요약", "월간 요약")
    
    val currentStats = if (selectedPeriod == 0) weeklyStats else monthlyStats
    val currentChartData = if (selectedPeriod == 0) weeklyChartData else monthlyChartData
    val hasData = if (selectedPeriod == 0) weeklyStats.totalStandardDrinks > 0 else monthlyStats.totalStandardDrinks > 0
    
    // 선택된 기간에 맞는 건강 상태 및 진행률 계산
    val isMale = true // TODO: 사용자 성별 가져오기, 기본값 남성
    val currentHealthStatus = if (selectedPeriod == 0) {
        // 주간 기준
        val weeklyAlcohol = weeklyStats.totalStandardDrinks * 8f // 표준잔 -> 알코올(g)
        when {
            weeklyAlcohol <= if (isMale) 196f else 98f -> DrinkingStatus.APPROPRIATE // 28g * 7일
            weeklyAlcohol <= if (isMale) 392f else 294f -> DrinkingStatus.CAUTION // 56g * 7일
            weeklyAlcohol <= if (isMale) 490f else 392f -> DrinkingStatus.EXCESSIVE // 70g * 7일
            else -> DrinkingStatus.DANGEROUS
        }
    } else {
        // 월간 기준
        val monthlyAlcohol = monthlyStats.totalStandardDrinks * 8f
        val daysInMonth = java.time.LocalDate.now().lengthOfMonth()
        when {
            monthlyAlcohol <= if (isMale) (28f * daysInMonth) else (14f * daysInMonth) -> DrinkingStatus.APPROPRIATE
            monthlyAlcohol <= if (isMale) (56f * daysInMonth) else (42f * daysInMonth) -> DrinkingStatus.CAUTION
            monthlyAlcohol <= if (isMale) (70f * daysInMonth) else (56f * daysInMonth) -> DrinkingStatus.EXCESSIVE
            else -> DrinkingStatus.DANGEROUS
        }
    }
    
    val progressValue = if (selectedPeriod == 0) {
        // 주간 진행률
        val weeklyAlcohol = weeklyStats.totalStandardDrinks * 8f
        val weeklyLimit = if (isMale) 196f else 98f // 주간 적정 한계
        (weeklyAlcohol / weeklyLimit).coerceAtMost(1f)
    } else {
        // 월간 진행률
        val monthlyAlcohol = monthlyStats.totalStandardDrinks * 8f
        val daysInMonth = java.time.LocalDate.now().lengthOfMonth()
        val monthlyLimit = if (isMale) (28f * daysInMonth) else (14f * daysInMonth)
        (monthlyAlcohol / monthlyLimit).coerceAtMost(1f)
    }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AppBackground)
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            periods.forEachIndexed { index, period ->
                FilterChip(
                    onClick = { viewModel.selectPeriod(index) },
                    label = { 
                        Box(
                            modifier = Modifier.fillMaxWidth(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = period,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    },
                    selected = selectedPeriod == index,
                    modifier = Modifier.weight(1f),
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = TabSelected.copy(alpha = 0.1f),
                        selectedLabelColor = TabSelected
                    )
                )
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // 캐릭터 코멘트 카드
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF4E5)),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
            shape = RoundedCornerShape(12.dp)
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "🐕",
                    fontSize = 32.sp,
                    modifier = Modifier.padding(end = 12.dp)
                )
                Text(
                    text = if (hasData) {
                        viewModel.getCharacterComment(currentHealthStatus)
                    } else {
                        "아직 기록이 없어요! 음주 기록을 추가해보세요."
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    color = TextPrimary
                )
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // 건강 지수 카드
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
                    
                    Spacer(modifier = Modifier.weight(1f))
                    
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = when(currentHealthStatus) {
                                DrinkingStatus.APPROPRIATE -> Color(0xFFE8F5E8)
                                DrinkingStatus.CAUTION -> Color(0xFFFFF4E5)
                                DrinkingStatus.EXCESSIVE -> Color(0xFFFDEBEC)
                                DrinkingStatus.DANGEROUS -> Color(0xFFFFE0E0)
                            }
                        ),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Text(
                            text = when(currentHealthStatus) {
                                DrinkingStatus.APPROPRIATE -> "양호"
                                DrinkingStatus.CAUTION -> "주의"
                                DrinkingStatus.EXCESSIVE -> "과음"
                                DrinkingStatus.DANGEROUS -> "위험"
                            },
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.SemiBold,
                            color = when(currentHealthStatus) {
                                DrinkingStatus.APPROPRIATE -> Color(0xFF2E7D32)
                                DrinkingStatus.CAUTION -> Color(0xFFE65100)
                                DrinkingStatus.EXCESSIVE -> Color(0xFFC62828)
                                DrinkingStatus.DANGEROUS -> Color.Black
                            },
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(12.dp))
                
                Text(
                    text = "현재 상태",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                LinearProgressIndicator(
                    progress = progressValue,
                    modifier = Modifier.fillMaxWidth(),
                    color = when(currentHealthStatus) {
                        DrinkingStatus.APPROPRIATE -> Color(0xFF4CAF50)
                        DrinkingStatus.CAUTION -> Color(0xFFFF9800)
                        DrinkingStatus.EXCESSIVE -> Color(0xFFF44336)
                        DrinkingStatus.DANGEROUS -> Color.Black
                    },
                    trackColor = AppBackground
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Text(
                    text = if (selectedPeriod == 0) {
                        "일평균 ${String.format("%.1f", weeklyStats.averagePerDay)}잔 (${String.format("%.1f", weeklyStats.totalStandardDrinks)}잔/30잔)"
                    } else {
                        "일평균 ${String.format("%.1f", monthlyStats.averagePerDay)}잔 (${String.format("%.1f", monthlyStats.totalStandardDrinks)}잔/30잔)"
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        if (hasData) {
            // 주별 음주량 트렌드 카드
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
                        text = if (selectedPeriod == 0) "주간 트렌드" else "월간 트렌드",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = TextPrimary
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    DrinkChart(
                        data = currentChartData,
                        isWeekly = selectedPeriod == 0,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // 술 종류별 통계 카드
            val currentDrinkTypeStats = if (selectedPeriod == 0) weeklyDrinkTypeStats else monthlyDrinkTypeStats
            if (currentDrinkTypeStats.isNotEmpty()) {
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
                            text = "술 종류별 통계",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = TextPrimary
                        )
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        currentDrinkTypeStats.take(5).forEach { (type, amount) ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = type.getEmoji(),
                                        fontSize = 16.sp
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = type.getDisplayName(),
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                }
                                
                                Row(
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    // 진행률 바
                                    val maxAmount = currentDrinkTypeStats.maxOfOrNull { it.second } ?: 1f
                                    val progress = (amount / maxAmount).coerceAtMost(1f)
                                    
                                    LinearProgressIndicator(
                                        progress = progress,
                                        modifier = Modifier
                                            .width(60.dp)
                                            .height(6.dp),
                                        color = when(type) {
                                            DrinkType.BEER -> Color(0xFFFFB74D)
                                            DrinkType.SOJU -> Color(0xFF64B5F6)
                                            else -> MaterialTheme.colorScheme.primary
                                        },
                                        trackColor = AppBackground
                                    )
                                    
                                    Spacer(modifier = Modifier.width(8.dp))
                                    
                                    Text(
                                        text = String.format("%.1f", amount),
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Medium,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
        
        // 얼굴 분석 결과 (월간에서만, 데이터 있을 때만)
        if (selectedPeriod == 1 && hasData) {
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
                            text = "얼굴 분석 결과 (월간)",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = TextPrimary
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "2",
                                style = MaterialTheme.typography.headlineLarge,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF4CAF50)
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
                                style = MaterialTheme.typography.headlineLarge,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF4CAF50)
                            )
                            Text(
                                text = "정확 확률",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextSecondary
                            )
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
        }
        
        // 하단 요약 카드들
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // 총 음주량 카드
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
            
            // 선호 음주 카드
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
                        } else if (selectedPeriod == 1 && monthlyStats.totalStandardDrinks > 0) {
                            // For monthly stats, calculate favorite type from current data
                            val currentDrinkTypeStats = monthlyDrinkTypeStats
                            if (currentDrinkTypeStats.isNotEmpty()) {
                                currentDrinkTypeStats.maxByOrNull { it.second }?.first?.getDisplayName() ?: "-"
                            } else {
                                "-"
                            }
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DatePickerDialog(
    onDismiss: () -> Unit,
    onDateSelected: (year: Int, month: Int, day: Int) -> Unit
) {
    val currentDate = LocalDate.now()
    var selectedYear by remember { mutableStateOf(currentDate.year) }
    var selectedMonth by remember { mutableStateOf(currentDate.monthValue) }
    var selectedDay by remember { mutableStateOf(currentDate.dayOfMonth) }
    
    var yearExpanded by remember { mutableStateOf(false) }
    var monthExpanded by remember { mutableStateOf(false) }
    var dayExpanded by remember { mutableStateOf(false) }
    
    val years = (2020..2030).toList()
    val months = (1..12).toList()
    val daysInMonth = java.time.YearMonth.of(selectedYear, selectedMonth).lengthOfMonth()
    val days = (1..daysInMonth).toList()
    
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = CardBackground)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "날짜 선택",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = TextPrimary
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Text(
                    text = "이동하고 싶은 연도, 월, 일을 입력하세요",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary,
                    textAlign = TextAlign.Center
                )
                
                Spacer(modifier = Modifier.height(24.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    // Year Dropdown
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "연도",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        ExposedDropdownMenuBox(
                            expanded = yearExpanded,
                            onExpandedChange = { yearExpanded = !yearExpanded }
                        ) {
                            OutlinedTextField(
                                value = selectedYear.toString(),
                                onValueChange = {},
                                readOnly = true,
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = yearExpanded) },
                                modifier = Modifier
                                    .menuAnchor()
                                    .width(80.dp),
                                colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors()
                            )
                            ExposedDropdownMenu(
                                expanded = yearExpanded,
                                onDismissRequest = { yearExpanded = false }
                            ) {
                                years.forEach { year ->
                                    DropdownMenuItem(
                                        text = { Text(year.toString()) },
                                        onClick = {
                                            selectedYear = year
                                            yearExpanded = false
                                        }
                                    )
                                }
                            }
                        }
                    }
                    
                    // Month Dropdown
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "월",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        ExposedDropdownMenuBox(
                            expanded = monthExpanded,
                            onExpandedChange = { monthExpanded = !monthExpanded }
                        ) {
                            OutlinedTextField(
                                value = selectedMonth.toString(),
                                onValueChange = {},
                                readOnly = true,
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = monthExpanded) },
                                modifier = Modifier
                                    .menuAnchor()
                                    .width(70.dp),
                                colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors()
                            )
                            ExposedDropdownMenu(
                                expanded = monthExpanded,
                                onDismissRequest = { monthExpanded = false }
                            ) {
                                months.forEach { month ->
                                    DropdownMenuItem(
                                        text = { Text(month.toString()) },
                                        onClick = {
                                            selectedMonth = month
                                            monthExpanded = false
                                        }
                                    )
                                }
                            }
                        }
                    }
                    
                    // Day Dropdown
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "일",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        ExposedDropdownMenuBox(
                            expanded = dayExpanded,
                            onExpandedChange = { dayExpanded = !dayExpanded }
                        ) {
                            OutlinedTextField(
                                value = selectedDay.toString(),
                                onValueChange = {},
                                readOnly = true,
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = dayExpanded) },
                                modifier = Modifier
                                    .menuAnchor()
                                    .width(70.dp),
                                colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors()
                            )
                            ExposedDropdownMenu(
                                expanded = dayExpanded,
                                onDismissRequest = { dayExpanded = false }
                            ) {
                                days.forEach { day ->
                                    DropdownMenuItem(
                                        text = { Text(day.toString()) },
                                        onClick = {
                                            selectedDay = day
                                            dayExpanded = false
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = AppBackground),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = "선택된 날짜\n${selectedYear}년 ${selectedMonth}월 ${selectedDay}일",
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Center,
                        color = TextPrimary,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp)
                    )
                }
                
                Spacer(modifier = Modifier.height(24.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = TextSecondary
                        )
                    ) {
                        Text("취소")
                    }
                    
                    Button(
                        onClick = { 
                            onDateSelected(selectedYear, selectedMonth, selectedDay)
                        },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.Black
                        )
                    ) {
                        Text(
                            text = "이동",
                            color = Color.White
                        )
                    }
                }
            }
        }
    }
}
