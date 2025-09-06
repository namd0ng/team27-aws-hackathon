package com.hackathon.alcolook.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hackathon.alcolook.data.*
import com.hackathon.alcolook.service.BedrockAnalysisService
import com.hackathon.alcolook.service.TestSensorDataGenerator
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class IntoxicationAnalysisViewModel(private val context: Context) : ViewModel() {
    
    private val bedrockService = BedrockAnalysisService(context)
    
    private val _uiState = MutableStateFlow(IntoxicationAnalysisUiState())
    val uiState: StateFlow<IntoxicationAnalysisUiState> = _uiState.asStateFlow()
    
    /**
     * 테스트 데이터로 분석 시작
     */
    fun startTestAnalysis(targetLevel: IntoxicationLevel? = null) {
        val testData = TestSensorDataGenerator.generateTestData(targetLevel)
        startAnalysis(testData)
    }
    
    /**
     * 실제 센서 데이터로 분석 시작
     */
    fun startAnalysis(sensorData: IntegratedSensorData) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isLoading = true,
                error = null
            )
            
            try {
                val report = bedrockService.analyzeIntoxication(sensorData = sensorData)
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    report = report,
                    error = null
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "분석 중 오류가 발생했습니다"
                )
            }
        }
    }
    
    /**
     * 결과 공유
     */
    fun shareResult() {
        val report = _uiState.value.report ?: return
        
        // TODO: 공유 기능 구현
        // 예: Intent를 통한 텍스트 공유
    }
    
    /**
     * 결과를 캘린더에 저장
     */
    fun saveToCalendar() {
        val report = _uiState.value.report ?: return
        
        viewModelScope.launch {
            try {
                // TODO: Room 데이터베이스에 저장
                // DrinkRecord로 변환하여 저장
                
                _uiState.value = _uiState.value.copy(
                    isSaved = true
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = "저장 중 오류가 발생했습니다: ${e.message}"
                )
            }
        }
    }
    
    /**
     * 다시 분석
     */
    fun retryAnalysis() {
        startTestAnalysis()
    }
    
    /**
     * 에러 상태 초기화
     */
    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
    
    /**
     * 저장 상태 초기화
     */
    fun clearSavedState() {
        _uiState.value = _uiState.value.copy(isSaved = false)
    }
}

data class IntoxicationAnalysisUiState(
    val isLoading: Boolean = false,
    val report: IntoxicationReport? = null,
    val error: String? = null,
    val isSaved: Boolean = false
)
