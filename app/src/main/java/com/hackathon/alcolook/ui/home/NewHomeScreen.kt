package com.hackathon.alcolook.ui.home

import android.net.Uri
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import com.hackathon.alcolook.data.HeartRateData

enum class MeasurementScreenState {
    METHOD_SELECTION,
    CAMERA_MEASUREMENT,
    PHOTO_UPLOAD,
    HEART_RATE_MEASUREMENT,
    GYROSCOPE_MEASUREMENT
}

@Composable
fun NewHomeScreen(
    navController: NavController? = null
) {
    var currentState by remember { mutableStateOf(MeasurementScreenState.METHOD_SELECTION) }
    var faceAnalysisResult by remember { mutableStateOf<Float?>(null) }
    var heartRateData by remember { mutableStateOf<HeartRateData?>(null) }
    
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
                    currentState = MeasurementScreenState.METHOD_SELECTION
                }
            )
        }
        
        MeasurementScreenState.GYROSCOPE_MEASUREMENT -> {
            // TODO: 자이로센서 측정 화면 구현 예정
            Text(
                text = "자이로센서 측정 화면 (구현 예정)",
                modifier = Modifier.fillMaxSize(),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
        }
    }
}
