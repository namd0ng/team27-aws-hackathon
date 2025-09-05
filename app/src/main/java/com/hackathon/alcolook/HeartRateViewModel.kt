package com.hackathon.alcolook

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class HeartRateViewModel : ViewModel() {
    
    private val _uiState = MutableStateFlow(HeartRateUiState())
    val uiState: StateFlow<HeartRateUiState> = _uiState.asStateFlow()
    
    fun updateHeartRate(bpm: Int) {
        _uiState.value = _uiState.value.copy(
            currentHeartRate = bpm,
            isLoading = false
        )
    }
    
    fun startMeasurement() {
        _uiState.value = _uiState.value.copy(isLoading = true)
        
        // 시뮬레이션: 3초 후 랜덤 심박수 생성
        viewModelScope.launch {
            kotlinx.coroutines.delay(3000)
            val randomBpm = (60..120).random()
            updateHeartRate(randomBpm)
        }
    }
    
    fun resetMeasurement() {
        _uiState.value = HeartRateUiState()
    }
}

data class HeartRateUiState(
    val currentHeartRate: Int = 0,
    val isLoading: Boolean = false,
    val restingHeartRate: Int = 70,
    val alcoholLevel: AlcoholLevel = AlcoholLevel.NONE,
    val userAge: Int = 30
)
