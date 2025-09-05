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
    
    // 건강 상태
    val healthStatus = weeklyStats.map { stats ->
        evaluateHealthStatus(stats.totalStandardDrinks, userProfile)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(), DrinkingStatus.NORMAL)
    
    // 주간 차트 데이터 (최근 7일)
    val weeklyChartData = records.map { recordList ->
        generateWeeklyChartData(recordList)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(), emptyList())
    
    // 월간 차트 데이터 (최근 4주)
    val monthlyChartData = records.map { recordList ->
        generateMonthlyChartData(recordList)
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
        val averagePerDay = if (daysInMonth > 0) totalStandardDrinks / daysInMonth else 0f
        
        return MonthlyStats(
            totalStandardDrinks = totalStandardDrinks,
            totalVolumeMl = totalVolumeMl,
            drinkingDays = drinkingDays,
            averagePerDay = averagePerDay
        )
    }
    
    private fun evaluateHealthStatus(weeklyStandardDrinks: Float, profile: UserProfile): DrinkingStatus {
        val weeklyLimit = when {
            profile.isSenior65 -> 7f // 65세 이상: 주 7잔
            profile.sex == Gender.MALE -> 14f // 남성: 주 14잔
            profile.sex == Gender.FEMALE -> 7f // 여성: 주 7잔
            else -> 14f // 기본값
        }
        
        return when {
            weeklyStandardDrinks <= weeklyLimit * 0.7f -> DrinkingStatus.NORMAL // 70% 이하: 양호
            weeklyStandardDrinks <= weeklyLimit -> DrinkingStatus.WARNING // 70~100%: 주의
            else -> DrinkingStatus.DANGER // 100% 초과: 위험
        }
    }
    
    fun getCharacterComment(status: DrinkingStatus): String {
        return when (status) {
            DrinkingStatus.NORMAL -> "🐕 멍멍! 적당한 음주 패턴이에요. 건강한 음주 습관을 유지하고 계시네요! 👏"
            DrinkingStatus.WARNING -> "🐕 멍멍! 조금 많이 마신 것 같아요. 오늘은 술 그만 마시는 게 어떨까요? 🤔"
            DrinkingStatus.DANGER -> "🐕 멍멍! 위험한 수준이에요. 건강을 위해 의사와 상담해보시는 걸 추천해요! ⚠️"
        }
    }
    
    private fun generateWeeklyChartData(records: List<DrinkRecord>): List<ChartData> {
        val today = LocalDate.now()
        val chartData = mutableListOf<ChartData>()
        
        // 최근 7일 (월~일)
        for (i in 6 downTo 0) {
            val date = today.minusDays(i.toLong())
            val dayRecords = records.filter { it.date == date }
            val totalStandardDrinks = dayRecords.sumOf { it.getStandardDrinks().toDouble() }.toFloat()
            
            val dayOfWeek = when (date.dayOfWeek.value) {
                1 -> "월"
                2 -> "화"
                3 -> "수"
                4 -> "목"
                5 -> "금"
                6 -> "토"
                7 -> "일"
                else -> ""
            }
            
            val status = evaluateDailyStatus(totalStandardDrinks)
            val color = when (status) {
                DrinkingStatus.NORMAL -> StatusNormal
                DrinkingStatus.WARNING -> StatusWarning
                DrinkingStatus.DANGER -> StatusDanger
            }
            
            chartData.add(
                ChartData(
                    label = dayOfWeek,
                    value = totalStandardDrinks,
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
        
        // 최근 4주
        for (week in 3 downTo 0) {
            val weekStart = today.minusWeeks(week.toLong()).with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
            val weekEnd = weekStart.plusDays(6)
            
            val weekRecords = records.filter { record ->
                !record.date.isBefore(weekStart) && !record.date.isAfter(weekEnd)
            }
            
            val totalStandardDrinks = weekRecords.sumOf { it.getStandardDrinks().toDouble() }.toFloat()
            
            val status = evaluateHealthStatus(totalStandardDrinks, userProfile)
            val color = when (status) {
                DrinkingStatus.NORMAL -> StatusNormal
                DrinkingStatus.WARNING -> StatusWarning
                DrinkingStatus.DANGER -> StatusDanger
            }
            
            chartData.add(
                ChartData(
                    label = "${4-week}주전",
                    value = totalStandardDrinks,
                    color = color,
                    status = status
                )
            )
        }
        
        return chartData
    }
    
    private fun evaluateDailyStatus(dailyStandardDrinks: Float): DrinkingStatus {
        val dailyLimit = when {
            userProfile.isSenior65 -> 1f // 65세 이상: 1잔
            userProfile.sex == Gender.MALE -> 2f // 남성: 2잔
            userProfile.sex == Gender.FEMALE -> 1f // 여성: 1잔
            else -> 2f // 기본값
        }
        
        return when {
            dailyStandardDrinks <= dailyLimit -> DrinkingStatus.NORMAL
            dailyStandardDrinks <= dailyLimit * 2 -> DrinkingStatus.WARNING
            else -> DrinkingStatus.DANGER
        }
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