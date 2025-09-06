package com.hackathon.alcolook.ui.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hackathon.alcolook.data.model.*
import com.hackathon.alcolook.data.repository.DrinkRecordRepository
import com.hackathon.alcolook.data.AuthManager
import okhttp3.MediaType.Companion.toMediaType
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.YearMonth
import javax.inject.Inject

@HiltViewModel
class CalendarViewModel @Inject constructor(
    private val drinkRecordRepository: DrinkRecordRepository,
    private val authManager: AuthManager
) : ViewModel() {

    private val _selectedDate = MutableStateFlow(LocalDate.now())
    val selectedDate = _selectedDate.asStateFlow()

    private val _currentMonth = MutableStateFlow(YearMonth.now())
    val currentMonth = _currentMonth.asStateFlow()

    private val _selectedTab = MutableStateFlow(0)
    val selectedTab = _selectedTab.asStateFlow()

    private val _selectedPeriod = MutableStateFlow(0) // 0: 주간, 1: 월간
    val selectedPeriod = _selectedPeriod.asStateFlow()

    // 모든 기록
    val allRecords = drinkRecordRepository.records
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(), emptyList())

    // 현재 월의 모든 기록
    val monthlyRecords = combine(allRecords, currentMonth) { records, month ->
        val startDate = month.atDay(1)
        val endDate = month.atEndOfMonth()
        records.filter { it.date >= startDate && it.date <= endDate }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(), emptyList())

    // 선택된 날짜의 기록
    val selectedDateRecords = combine(allRecords, selectedDate) { records, date ->
        records.filter { it.date == date }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(), emptyList())

    // 날짜별 상태 맵
    val dailyStatusMap = monthlyRecords.map { records ->
        records.groupBy { it.date }
            .mapValues { (_, dayRecords) ->
                getDailyStatus(dayRecords)
            }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(), emptyMap())

    // 선택된 날짜 요약
    val selectedDateSummary = selectedDateRecords.map { records ->
        DailySummary(
            date = selectedDate.value,
            records = records,
            totalStandardDrinks = records.sumOf { it.getStandardDrinks().toDouble() }.toFloat(),
            totalVolumeMl = records.sumOf { it.totalVolumeMl },
            status = getDailyStatus(records)
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
            val totalVolume = customVolumeMl ?: (unit.getVolumeMl(type) * quantity)
            val record = DrinkRecord(
                date = selectedDate.value,
                type = type,
                unit = unit,
                quantity = quantity,
                totalVolumeMl = totalVolume,
                abv = customAbv,
                note = note
            )
            
            Log.d("CalendarViewModel", "Adding drink record: $record")
            drinkRecordRepository.addRecord(record)
        }
    }
    
    fun simpleTest() {
        viewModelScope.launch {
            Log.d("CalendarViewModel", "🔥 Simple test starting...")
            
            try {
                val json = """{"userId":"app-test","date":"2024-01-20","drinkType":"BEER","count":1,"volumeMl":355,"abv":4.5,"note":"simple test"}"""
                
                val client = okhttp3.OkHttpClient()
                val body = okhttp3.RequestBody.create(
                    null, 
                    json
                )
                val request = okhttp3.Request.Builder()
                    .url("https://iql82o9kv2.execute-api.us-east-1.amazonaws.com/prod/records")
                    .addHeader("Content-Type", "application/json")
                    .post(body)
                    .build()
                
                val response = client.newCall(request).execute()
                Log.d("CalendarViewModel", "📱 Response: ${response.code} - ${response.body?.string()}")
                
            } catch (e: Exception) {
                Log.e("CalendarViewModel", "💥 Error: $e")
            }
        }
    }

    fun updateDrinkRecord(record: DrinkRecord) {
        viewModelScope.launch {
            drinkRecordRepository.updateRecord(record)
        }
    }

    fun deleteDrinkRecord(recordId: Long) {
        viewModelScope.launch {
            drinkRecordRepository.deleteRecord(recordId)
        }
    }

    private fun getDailyStatus(records: List<DrinkRecord>): DrinkingStatus {
        val totalStandardDrinks = records.sumOf { it.getStandardDrinks().toDouble() }.toFloat()
        return when {
            totalStandardDrinks <= 2f -> DrinkingStatus.NORMAL
            totalStandardDrinks <= 5f -> DrinkingStatus.WARNING
            else -> DrinkingStatus.DANGER
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
