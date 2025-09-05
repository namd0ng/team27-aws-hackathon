package com.hackathon.alcolook

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

class WalkingTestViewModel(context: Context) : ViewModel() {
    
    private val gyroscopeManager = GyroscopeManager(context)
    
    private val _uiState = MutableStateFlow(WalkingTestUiState())
    val uiState: StateFlow<WalkingTestUiState> = _uiState
    
    init {
        viewModelScope.launch {
            combine(
                gyroscopeManager.walkingData,
                _uiState
            ) { walkingData, uiState ->
                uiState.copy(
                    walkingData = walkingData,
                    alcoholLevel = calculateAlcoholLevel(walkingData.score),
                    isGyroscopeAvailable = gyroscopeManager.isGyroscopeAvailable()
                )
            }.collect {
                _uiState.value = it
            }
        }
    }
    
    fun startTest() {
        gyroscopeManager.startTest()
    }
    
    fun stopTest() {
        gyroscopeManager.stopTest()
    }
    
    fun nextPhase() {
        gyroscopeManager.nextPhase()
    }
    
    override fun onCleared() {
        super.onCleared()
        gyroscopeManager.cleanup()
    }
    
    private fun calculateAlcoholLevel(score: Int): AlcoholLevel {
        return when {
            score >= 80 -> AlcoholLevel.NORMAL
            score >= 60 -> AlcoholLevel.MILD
            score >= 40 -> AlcoholLevel.MODERATE
            else -> AlcoholLevel.HIGH
        }
    }
}

data class WalkingTestUiState(
    val walkingData: WalkingTestData = WalkingTestData(),
    val alcoholLevel: AlcoholLevel = AlcoholLevel.UNKNOWN,
    val isGyroscopeAvailable: Boolean = false
)
