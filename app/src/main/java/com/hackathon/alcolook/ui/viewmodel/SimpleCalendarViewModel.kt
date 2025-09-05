package com.hackathon.alcolook.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hackathon.alcolook.data.model.*
import com.hackathon.alcolook.ui.components.ChartData
import com.hackathon.alcolook.ui.theme.*
import kotlinx.coroutines.flow.*
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import java.time.temporal.TemporalAdjusters
import kotlin.math.roundToInt

class SimpleCalendarViewModel : ViewModel() {

    private val _selectedDate = MutableStateFlow(LocalDate.now())
    val selectedDate = _selectedDate.asStateFlow()

    private val _currentMonth = MutableStateFlow(YearMonth.now())
    val currentMonth = _currentMonth.asStateFlow()

    private val _selectedTab = MutableStateFlow(0)
    val selectedTab = _selectedTab.asStateFlow()

    private val _selectedPeriod = MutableStateFlow(0)
    val selectedPeriod = _selectedPeriod.asStateFlow()

    // 테스트용 더미 데이터
    private val _records = MutableStateFlow(listOf<DrinkRecord>())
    val records = _records.asStateFlow()
    
    // 사용자 프로필 (임시로 남성 기본값)
    private val userProfile = UserProfile(
        sex = Gender.MALE,
        isSenior65 = false,
        weeklyGoalStdDrinks = 14
    )
    
    // 주간 통계 (월~일)
    val weeklyStats = records.map { recordList ->
        val today = LocalDate.now()
        val startOfWeek = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
        val endOfWeek = today.with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY))
        
        val weeklyRecords = recordList.filter { record ->
            !record.date.isBefore(startOfWeek) && !record.date.isAfter(endOfWeek)
        }
        
        calculateWeeklyStats(weeklyRecords)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(), WeeklyStats())
    
    // 월간 통계
    val monthlyStats = records.map { recordList ->
        val today = LocalDate.now()
        val startOfMonth = today.withDayOfMonth(1)
        val endOfMonth = today.withDayOfMonth(today.lengthOfMonth())
        
        val monthlyRecords = recordList.filter { record ->
            !record.date.isBefore(startOfMonth) && !record.date.isAfter(endOfMonth)
        }
        
        calculateMonthlyStats(monthlyRecords)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(), MonthlyStats())
    
    // 날짜별 상태 맵
    val dailyStatusMap = records.map { recordList ->
        val statusMap = mutableMapOf<LocalDate, DrinkingStatus>()
        val isMale = userProfile.sex == Gender.MALE
        
        // 월간 과음 횟수 계산
        val currentMonth = LocalDate.now().withDayOfMonth(1)
        val monthlyExcessiveDays = recordList.filter { record ->
            record.date >= currentMonth && record.date < currentMonth.plusMonths(1)
        }.groupBy { it.date }.count { (_, dayRecords) ->
            val dailyAlcohol = dayRecords.sumOf { it.getPureAlcoholGrams().toDouble() }.toFloat()
            dailyAlcohol > if (isMale) 70f else 56f
        }
        
        // 각 날짜별 상태 계산
        recordList.groupBy { it.date }.forEach { (date, dayRecords) ->
            val dailyAlcohol = dayRecords.sumOf { it.getPureAlcoholGrams().toDouble() }.toFloat()
            val dailyStatus = evaluateDailyStatus(dailyAlcohol, isMale)
            val monthlyStatus = evaluateMonthlyStatus(monthlyExcessiveDays)
            statusMap[date] = evaluateOverallHealthStatus(dailyStatus, monthlyStatus)
        }
        
        statusMap
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(), emptyMap())
    
    // 건강 상태 (오늘 기준)
    val healthStatus = dailyStatusMap.map { statusMap ->
        statusMap[LocalDate.now()] ?: DrinkingStatus.APPROPRIATE
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(), DrinkingStatus.APPROPRIATE)
    
    // 주간 차트 데이터 (최근 7일)
    val weeklyChartData = records.map { recordList ->
        generateWeeklyChartData(recordList)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(), emptyList())
    
    // 월간 차트 데이터 (최근 4주)
    val monthlyChartData = records.map { recordList ->
        generateMonthlyChartData(recordList)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(), emptyList())
    
    // 주간 술 종류별 통계
    val weeklyDrinkTypeStats = records.map { recordList ->
        val today = LocalDate.now()
        val startOfWeek = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
        val endOfWeek = today.with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY))
        
        val weeklyRecords = recordList.filter { record ->
            !record.date.isBefore(startOfWeek) && !record.date.isAfter(endOfWeek)
        }
        
        weeklyRecords.groupBy { it.type }
            .mapValues { (_, records) -> 
                records.sumOf { it.getPureAlcoholGrams().toDouble() }.toFloat()
            }
            .toList()
            .sortedByDescending { it.second }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(), emptyList())
    
    // 월간 술 종류별 통계
    val monthlyDrinkTypeStats = records.map { recordList ->
        val today = LocalDate.now()
        val startOfMonth = today.withDayOfMonth(1)
        val endOfMonth = today.withDayOfMonth(today.lengthOfMonth())
        
        val monthlyRecords = recordList.filter { record ->
            !record.date.isBefore(startOfMonth) && !record.date.isAfter(endOfMonth)
        }
        
        monthlyRecords.groupBy { it.type }
            .mapValues { (_, records) -> 
                records.sumOf { it.getPureAlcoholGrams().toDouble() }.toFloat()
            }
            .toList()
            .sortedByDescending { it.second }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(), emptyList())

    fun selectDate(date: LocalDate) {
        _selectedDate.value = date
    }

    fun selectTab(tabIndex: Int) {
        _selectedTab.value = tabIndex
    }

    fun selectPeriod(periodIndex: Int) {
        _selectedPeriod.value = periodIndex
    }
    fun goToPreviousMonth() {
        _currentMonth.value = _currentMonth.value.minusMonths(1)
    }

    fun goToNextMonth() {
        _currentMonth.value = _currentMonth.value.plusMonths(1)
    }
    fun addDrinkRecord(
        type: DrinkType,
        unit: DrinkUnit,
        quantity: Int,
        customAbv: Float? = null,
        customDrinkName: String? = null,
        note: String? = null
    ) {
        val volumePerUnit = unit.getVolumeMl(type)
        val totalVolume = volumePerUnit * quantity
        val actualAbv = customAbv ?: type.getDefaultAbv()
        
        val newRecord = DrinkRecord(
            id = System.currentTimeMillis(),
            date = selectedDate.value,
            type = type,
            unit = unit,
            quantity = quantity,
            totalVolumeMl = totalVolume,
            abv = customAbv, // null이면 기본값 사용
            note = note
        )
        _records.value = _records.value + newRecord
    }
    
    fun updateDrinkRecord(
        recordId: Long,
        type: DrinkType,
        unit: DrinkUnit,
        quantity: Int,
        customAbv: Float? = null,
        note: String? = null
    ) {
        val volumePerUnit = unit.getVolumeMl(type)
        val totalVolume = volumePerUnit * quantity
        
        _records.value = _records.value.map { record ->
            if (record.id == recordId) {
                record.copy(
                    type = type,
                    unit = unit,
                    quantity = quantity,
                    totalVolumeMl = totalVolume,
                    abv = customAbv,
                    note = note
                )
            } else record
        }
    }
    
    fun deleteDrinkRecord(recordId: Long) {
        _records.value = _records.value.filter { it.id != recordId }
    }
    
    private fun calculateWeeklyStats(records: List<DrinkRecord>): WeeklyStats {
        if (records.isEmpty()) return WeeklyStats()
        
        val totalStandardDrinks = records.sumOf { it.getStandardDrinks().toDouble() }.toFloat()
        val totalVolumeMl = records.sumOf { it.totalVolumeMl }
        val drinkingDays = records.map { it.date }.distinct().size
        val averagePerDay = if (drinkingDays > 0) totalStandardDrinks / 7f else 0f
        
        // 가장 많이 마신 술 종류
        val favoriteType = records.groupBy { it.type }
            .maxByOrNull { it.value.sumOf { record -> record.getStandardDrinks().toDouble() } }
            ?.key ?: DrinkType.BEER
        
        return WeeklyStats(
            totalStandardDrinks = totalStandardDrinks,
            totalVolumeMl = totalVolumeMl,
            drinkingDays = drinkingDays,
            averagePerDay = averagePerDay,
            favoriteType = favoriteType
        )
    }
    
    private fun calculateMonthlyStats(records: List<DrinkRecord>): MonthlyStats {
        if (records.isEmpty()) return MonthlyStats()
        
        val totalStandardDrinks = records.sumOf { it.getStandardDrinks().toDouble() }.toFloat()
        val totalVolumeMl = records.sumOf { it.totalVolumeMl }
        val drinkingDays = records.map { it.date }.distinct().size
        val daysInMonth = LocalDate.now().lengthOfMonth()
        val averagePerDay = totalStandardDrinks / daysInMonth // 전체 월 일수로 나누기
        
        return MonthlyStats(
            totalStandardDrinks = totalStandardDrinks,
            totalVolumeMl = totalVolumeMl,
            drinkingDays = drinkingDays,
            averagePerDay = averagePerDay
        )
    }
    
    // 일일 알코올 섭취량 평가
    private fun evaluateDailyStatus(dailyAlcoholGrams: Float, isMale: Boolean): DrinkingStatus {
        return when {
            dailyAlcoholGrams <= if (isMale) 28f else 14f -> DrinkingStatus.APPROPRIATE
            dailyAlcoholGrams <= if (isMale) 56f else 42f -> DrinkingStatus.CAUTION  
            dailyAlcoholGrams <= if (isMale) 70f else 56f -> DrinkingStatus.EXCESSIVE
            else -> DrinkingStatus.EXCESSIVE // 일일 기준으로는 최대 EXCESSIVE
        }
    }
    
    // 월간 과음 횟수 기준 평가
    private fun evaluateMonthlyStatus(monthlyExcessiveDays: Int): DrinkingStatus {
        return if (monthlyExcessiveDays >= 5) DrinkingStatus.DANGEROUS else DrinkingStatus.APPROPRIATE
    }
    
    // 전체 건강 상태 평가 (가장 높은 위험도 반환)
    private fun evaluateOverallHealthStatus(dailyStatus: DrinkingStatus, monthlyStatus: DrinkingStatus): DrinkingStatus {
        return when {
            dailyStatus == DrinkingStatus.DANGEROUS || monthlyStatus == DrinkingStatus.DANGEROUS -> DrinkingStatus.DANGEROUS
            dailyStatus == DrinkingStatus.EXCESSIVE -> DrinkingStatus.EXCESSIVE
            dailyStatus == DrinkingStatus.CAUTION -> DrinkingStatus.CAUTION
            else -> DrinkingStatus.APPROPRIATE
        }
    }
    
    fun getCharacterComment(status: DrinkingStatus): String {
        val comments = when (status) {
            DrinkingStatus.APPROPRIATE -> listOf(
                "오늘은 딱 알맞게 즐기셨네요! 균형 잡힌 음주, 멋져요!",
                "좋습니다 내일도 상쾌하게 일어날 수 있겠네요.",
                "이 정도면 건강에 큰 무리 없어요. 현명한 선택이네요!",
                "오늘은 깔끔하게 딱 적정량만! 자기 관리 잘하시네요"
            )
            DrinkingStatus.CAUTION -> listOf(
                "조금은 과했네요 내일은 물 많이 드시고 쉬어주세요.",
                "이 정도면 괜찮지만, 매일 반복되면 몸이 힘들 수 있어요.",
                "슬슬 간이 피곤해질지도… 내일은 가볍게 보내는 게 어떨까요?",
                "컨디션 체크하면서 마시는 것도 중요해요"
            )
            DrinkingStatus.EXCESSIVE -> listOf(
                "이건 위험한 수준이에요 속도를 줄이셔야 합니다.",
                "오늘은 좀 과격했네요… 간이 놀랐을 거예요",
                "이러다 내일 숙취와 함께 고통받을 수도 있어요",
                "가끔은 괜찮지만, 자주 반복되면 건강에 큰 부담이 돼요."
            )
            DrinkingStatus.DANGEROUS -> listOf(
                "심각한 음주 패턴이 보입니다 전문가 상담을 고려하세요.",
                "몸이 보내는 신호를 무시하지 마세요. 위험해요.",
                "이 정도면 간이 SOS를 보내고 있을 거예요",
                "한 번쯤 음주 습관을 점검해보는 게 어떨까요?",
                "과음은 삶의 질을 떨어뜨립니다. 지금이 바꿀 때예요."
            )
        }
        return "🐕 " + comments.random()
    }
    
    private fun generateWeeklyChartData(records: List<DrinkRecord>): List<ChartData> {
        val today = LocalDate.now()
        val chartData = mutableListOf<ChartData>()
        val isMale = userProfile.sex == Gender.MALE
        
        // 이번 주 월요일부터 일요일까지 (현재 날짜 기준)
        val startOfWeek = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
        
        for (i in 0..6) {
            val date = startOfWeek.plusDays(i.toLong())
            val dayRecords = records.filter { it.date == date }
            val totalAlcohol = dayRecords.sumOf { it.getPureAlcoholGrams().toDouble() }.toFloat()
            
            val dayOfWeek = when (i) {
                0 -> "월"
                1 -> "화"
                2 -> "수"
                3 -> "목"
                4 -> "금"
                5 -> "토"
                6 -> "일"
                else -> ""
            }
            
            val status = evaluateDailyStatus(totalAlcohol, isMale)
            val color = when (status) {
                DrinkingStatus.APPROPRIATE -> androidx.compose.ui.graphics.Color.Green
                DrinkingStatus.CAUTION -> androidx.compose.ui.graphics.Color(0xFFFF9800)
                DrinkingStatus.EXCESSIVE -> androidx.compose.ui.graphics.Color.Red
                DrinkingStatus.DANGEROUS -> androidx.compose.ui.graphics.Color.Black
            }
            
            chartData.add(
                ChartData(
                    label = dayOfWeek,
                    value = totalAlcohol,
                    color = color,
                    status = status
                )
            )
        }
        
        return chartData
    }
    
    private fun generateMonthlyChartData(records: List<DrinkRecord>): List<ChartData> {
        val today = LocalDate.now()
        val chartData = mutableListOf<ChartData>()
        val isMale = userProfile.sex == Gender.MALE
        
        // 이번 달의 주차별 데이터 (월~일 기준)
        val startOfMonth = today.withDayOfMonth(1)
        val endOfMonth = today.withDayOfMonth(today.lengthOfMonth())
        
        // 이번 달의 모든 월요일 찾기
        val mondays = mutableListOf<LocalDate>()
        var currentMonday = startOfMonth.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
        
        while (currentMonday.isBefore(endOfMonth.plusDays(1))) {
            if (!currentMonday.isBefore(startOfMonth) || currentMonday.plusDays(6).isAfter(startOfMonth.minusDays(1))) {
                mondays.add(currentMonday)
            }
            currentMonday = currentMonday.plusWeeks(1)
        }
        
        mondays.forEachIndexed { index, weekStart ->
            val weekEnd = weekStart.plusDays(6)
            
            // 이번 달에 포함되는 날짜들만 필터링
            val weekRecords = records.filter { record ->
                !record.date.isBefore(weekStart) && 
                !record.date.isAfter(weekEnd) &&
                !record.date.isBefore(startOfMonth) &&
                !record.date.isAfter(endOfMonth)
            }
            
            val totalAlcohol = weekRecords.sumOf { it.getPureAlcoholGrams().toDouble() }.toFloat()
            
            val status = when {
                totalAlcohol <= if (isMale) 196f else 98f -> DrinkingStatus.APPROPRIATE // 28g * 7일
                totalAlcohol <= if (isMale) 392f else 294f -> DrinkingStatus.CAUTION
                totalAlcohol <= if (isMale) 490f else 392f -> DrinkingStatus.EXCESSIVE
                else -> DrinkingStatus.DANGEROUS
            }
            
            val color = when (status) {
                DrinkingStatus.APPROPRIATE -> androidx.compose.ui.graphics.Color.Green
                DrinkingStatus.CAUTION -> androidx.compose.ui.graphics.Color(0xFFFF9800)
                DrinkingStatus.EXCESSIVE -> androidx.compose.ui.graphics.Color.Red
                DrinkingStatus.DANGEROUS -> androidx.compose.ui.graphics.Color.Black
            }
            
            chartData.add(
                ChartData(
                    label = "${index + 1}주차",
                    value = totalAlcohol,
                    color = color,
                    status = status
                )
            )
        }
        
        return chartData
    }
    

}

// 통계 데이터 클래스들
data class WeeklyStats(
    val totalStandardDrinks: Float = 0f,
    val totalVolumeMl: Int = 0,
    val drinkingDays: Int = 0,
    val averagePerDay: Float = 0f,
    val favoriteType: DrinkType = DrinkType.BEER
)

data class MonthlyStats(
    val totalStandardDrinks: Float = 0f,
    val totalVolumeMl: Int = 0,
    val drinkingDays: Int = 0,
    val averagePerDay: Float = 0f
)
