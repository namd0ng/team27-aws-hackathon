package com.hackathon.alcolook

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlin.math.abs

class HeartRateViewModel(context: Context) : ViewModel() {
    
    private val healthConnectManager = HealthConnectManager(context)
    
    private val _uiState = MutableStateFlow(HeartRateUiState())
    val uiState: StateFlow<HeartRateUiState> = _uiState
    
    fun checkPermissions() {
        viewModelScope.launch {
            val hasPermissions = healthConnectManager.hasAllPermissions()
            _uiState.value = _uiState.value.copy(hasPermissions = hasPermissions)
        }
    }
    
    fun measureHeartRate(userAge: Int = 30) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            
            val heartRateData = healthConnectManager.getRecentHeartRate()
            val baselineHeartRate = healthConnectManager.getBaselineHeartRate(userAge)
            
            val alcoholLevel = calculateAlcoholLevel(heartRateData?.bpm, baselineHeartRate)
            
            _uiState.value = _uiState.value.copy(
                isLoading = false,
                heartRateData = heartRateData,
                baselineHeartRate = baselineHeartRate,
                alcoholLevel = alcoholLevel,
                userAge = userAge
            )
        }
    }
    
    private fun calculateAlcoholLevel(currentBpm: Long?, baselineBpm: Double?): AlcoholLevel {
        if (currentBpm == null || baselineBpm == null) {
            return AlcoholLevel.UNKNOWN
        }
        
        val difference = abs(currentBpm - baselineBpm)
        
        return when {
            difference < 10 -> AlcoholLevel.NORMAL
            difference < 20 -> AlcoholLevel.MILD
            difference < 30 -> AlcoholLevel.MODERATE
            else -> AlcoholLevel.HIGH
        }
    }
}

data class HeartRateUiState(
    val isLoading: Boolean = false,
    val hasPermissions: Boolean = false,
    val heartRateData: HeartRateData? = null,
    val baselineHeartRate: Double? = null,
    val alcoholLevel: AlcoholLevel = AlcoholLevel.UNKNOWN,
    val userAge: Int = 30
)

enum class AlcoholLevel {
    NORMAL, MILD, MODERATE, HIGH, UNKNOWN
}
