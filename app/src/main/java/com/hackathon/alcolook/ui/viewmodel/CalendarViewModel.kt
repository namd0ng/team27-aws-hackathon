package com.hackathon.alcolook.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hackathon.alcolook.data.model.*
import com.hackathon.alcolook.data.repository.DrinkRecordRepository
import com.hackathon.alcolook.data.repository.UserProfileRepository
import com.hackathon.alcolook.domain.usecase.DrinkCalculationUseCase
import com.hackathon.alcolook.domain.usecase.DrinkingStatus
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.YearMonth
import javax.inject.Inject

@HiltViewModel
class CalendarViewModel @Inject constructor(
    private val drinkRecordRepository: DrinkRecordRepository,
    private val userProfileRepository: UserProfileRepository,
    private val drinkCalculationUseCase: DrinkCalculationUseCase
) : ViewModel() {

    private val _selectedDate = MutableStateFlow(LocalDate.now())
    val selectedDate = _selectedDate.asStateFlow()

    private val _currentMonth = MutableStateFlow(YearMonth.now())
    val currentMonth = _currentMonth.asStateFlow()

    private val _selectedTab = MutableStateFlow(0)
    val selectedTab = _selectedTab.asStateFlow()

    private val _selectedPeriod = MutableStateFlow(0) // 0: 주간, 1: 월간
    val selectedPeriod = _selectedPeriod.asStateFlow()

    val userProfile = userProfileRepository.userProfile
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(), UserProfile())

    // 현재 월의 모든 기록
    val monthlyRecords = currentMonth.flatMapLatest { month ->
        val startDate = month.atDay(1)
        val endDate = month.atEndOfMonth()
        drinkRecordRepository.getRecordsBetweenDates(startDate, endDate)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(), emptyList())

    // 선택된 날짜의 기록
    val selectedDateRecords = selectedDate.flatMapLatest { date ->
        drinkRecordRepository.getRecordsByDate(date)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(), emptyList())

    // 날짜별 상태 맵
    val dailyStatusMap = combine(monthlyRecords, userProfile) { records, profile ->
        records.groupBy { it.date }
            .mapValues { (_, dayRecords) ->
                drinkCalculationUseCase.getDailyStatus(dayRecords, profile)
            }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(), emptyMap())

    // 선택된 날짜 요약
    val selectedDateSummary = combine(selectedDateRecords, userProfile) { records, profile ->
        DailySummary(
            date = selectedDate.value,
            records = records,
            totalStandardDrinks = records.sumOf { it.getStandardDrinks().toDouble() }.toFloat(),
            totalVolumeMl = records.sumOf { it.totalVolumeMl },
            status = drinkCalculationUseCase.getDailyStatus(records, profile)
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(), 
        DailySummary(LocalDate.now(), emptyList(), 0f, 0, DrinkingStatus.NORMAL))

    fun selectDate(date: LocalDate) {
        _selectedDate.value = date
    }

    fun selectTab(tabIndex: Int) {
        _selectedTab.value = tabIndex
    }

    fun selectPeriod(periodIndex: Int) {
        _selectedPeriod.value = periodIndex
    }

    fun changeMonth(month: YearMonth) {
        _currentMonth.value = month
    }

    fun addDrinkRecord(
        type: DrinkType,
        unit: DrinkUnit,
        quantity: Int,
        customVolumeMl: Int? = null,
        customAbv: Float? = null,
        note: String? = null
    ) {
        viewModelScope.launch {
            val record = drinkCalculationUseCase.createDrinkRecord(
                date = selectedDate.value,
                type = type,
                unit = unit,
                quantity = quantity,
                customVolumeMl = customVolumeMl,
                customAbv = customAbv,
                note = note
            )
            drinkRecordRepository.insertRecord(record)
        }
    }

    fun updateDrinkRecord(record: DrinkRecord) {
        viewModelScope.launch {
            drinkRecordRepository.updateRecord(record)
        }
    }

    fun deleteDrinkRecord(record: DrinkRecord) {
        viewModelScope.launch {
            drinkRecordRepository.deleteRecord(record)
        }
    }
}

data class DailySummary(
    val date: LocalDate,
    val records: List<DrinkRecord>,
    val totalStandardDrinks: Float,
    val totalVolumeMl: Int,
    val status: DrinkingStatus
)