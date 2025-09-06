package com.hackathon.alcolook.ui.home

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import com.hackathon.alcolook.data.HeartRateData
import com.hackathon.alcolook.data.GyroscopeData

enum class MeasurementScreenState {
    METHOD_SELECTION,
    CAMERA_MEASUREMENT,
    PHOTO_UPLOAD,
    HEART_RATE_MEASUREMENT,
    GYROSCOPE_MEASUREMENT,
    ANALYSIS_RESULT
}

@Composable
fun NewHomeScreen(
    navController: NavController? = null
) {
    var currentState by remember { mutableStateOf(MeasurementScreenState.METHOD_SELECTION) }
    var faceAnalysisResult by remember { mutableStateOf<Float?>(null) }
    var heartRateData by remember { mutableStateOf<HeartRateData?>(null) }
    var gyroscopeData by remember { mutableStateOf<GyroscopeData?>(null) }
    
    when (currentState) {
        MeasurementScreenState.METHOD_SELECTION -> {
            MeasurementMethodScreen(
                onCameraClick = {
                    currentState = MeasurementScreenState.CAMERA_MEASUREMENT
                },
                onPhotoUploadClick = {
                    currentState = MeasurementScreenState.PHOTO_UPLOAD
                }
            )
        }
        
        MeasurementScreenState.CAMERA_MEASUREMENT -> {
            CameraScreenWithFace(
                onMeasureClick = { percentage ->
                    faceAnalysisResult = percentage
                    currentState = MeasurementScreenState.HEART_RATE_MEASUREMENT
                },
                onBackClick = {
                    currentState = MeasurementScreenState.METHOD_SELECTION
                }
            )
        }
        
        MeasurementScreenState.PHOTO_UPLOAD -> {
            PhotoUploadScreenWithAnalysis(
                onNextClick = { percentage ->
                    faceAnalysisResult = percentage
                    currentState = MeasurementScreenState.HEART_RATE_MEASUREMENT
                },
                onBackClick = {
                    currentState = MeasurementScreenState.METHOD_SELECTION
                }
            )
        }
        
        MeasurementScreenState.HEART_RATE_MEASUREMENT -> {
            HeartRateScreen(
                faceAnalysisResult = faceAnalysisResult,
                onNextClick = { heartRate ->
                    heartRateData = heartRate
                    currentState = MeasurementScreenState.GYROSCOPE_MEASUREMENT
                },
                onBackClick = {
                    currentState = MeasurementScreenState.CAMERA_MEASUREMENT
                }
            )
        }
        
        MeasurementScreenState.GYROSCOPE_MEASUREMENT -> {
            GyroscopeScreen(
                faceAnalysisResult = faceAnalysisResult,
                heartRateData = heartRateData,
                onNextClick = { gyroData ->
                    gyroscopeData = gyroData
                    currentState = MeasurementScreenState.ANALYSIS_RESULT
                },
                onBackClick = {
                    currentState = MeasurementScreenState.HEART_RATE_MEASUREMENT
                }
            )
        }
        
        MeasurementScreenState.ANALYSIS_RESULT -> {
            ComprehensiveAnalysisScreen(
                faceAnalysisResult = faceAnalysisResult,
                heartRateData = heartRateData,
                gyroscopeData = gyroscopeData,
                onBackClick = {
                    // 처음부터 다시 시작
                    faceAnalysisResult = null
                    heartRateData = null
                    gyroscopeData = null
                    currentState = MeasurementScreenState.METHOD_SELECTION
                },
                onSaveClick = {
                    // TODO: 캘린더에 결과 저장
                    // 임시로 처음으로 돌아가기
                    faceAnalysisResult = null
                    heartRateData = null
                    gyroscopeData = null
                    currentState = MeasurementScreenState.METHOD_SELECTION
                }
            )
        }
    }
}
