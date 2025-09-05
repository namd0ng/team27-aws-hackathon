package com.hackathon.alcolook

import android.Manifest
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.hackathon.alcolook.ui.theme.AlcoLookTheme
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AlcoLookTheme {
                DrunkDetectionScreen()
            }
        }
    }
}

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun DrunkDetectionScreen() {
    val cameraPermissionState = rememberPermissionState(Manifest.permission.CAMERA)
    var drunkPercentage by remember { mutableStateOf(0) }
    var isAnalyzing by remember { mutableStateOf(false) }
    val drunkDetectionService = remember { DrunkDetectionService() }
    val coroutineScope = rememberCoroutineScope()
    
    if (cameraPermissionState.isGranted) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black)
        ) {
            // 카메라 미리보기
            CameraPreview(
                onImageCaptured = { bitmap ->
                    if (!isAnalyzing) {
                        isAnalyzing = true
                        coroutineScope.launch {
                            try {
                                val result = drunkDetectionService.detectDrunkLevel(bitmap)
                                drunkPercentage = result
                            } finally {
                                isAnalyzing = false
                            }
                        }
                    }
                },
                modifier = Modifier.fillMaxSize()
            )
            
            // 결과 표시 오버레이
            Column(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(32.dp)
                    .background(
                        Color.Black.copy(alpha = 0.7f),
                        shape = MaterialTheme.shapes.large
                    )
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "음주 감지 결과",
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // 퍼센트 표시
                Box(
                    modifier = Modifier
                        .size(120.dp)
                        .clip(CircleShape)
                        .background(getColorForPercentage(drunkPercentage)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "${drunkPercentage}%",
                        color = Color.White,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Text(
                    text = getDrunkMessage(drunkPercentage),
                    color = Color.White,
                    fontSize = 16.sp,
                    textAlign = TextAlign.Center
                )
                
                if (isAnalyzing) {
                    Spacer(modifier = Modifier.height(8.dp))
                    CircularProgressIndicator(
                        color = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }
    } else {
        // 권한 요청 화면
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "카메라 권한이 필요합니다",
                fontSize = 18.sp,
                textAlign = TextAlign.Center
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Button(
                onClick = { cameraPermissionState.launchPermissionRequest() }
            ) {
                Text("권한 허용")
            }
        }
    }
}

private fun getColorForPercentage(percentage: Int): Color {
    return when {
        percentage < 30 -> Color.Green
        percentage < 60 -> Color.Yellow
        else -> Color.Red
    }
}

private fun getDrunkMessage(percentage: Int): String {
    return when {
        percentage < 20 -> "아직 괜찮아요!"
        percentage < 40 -> "조금 취한 것 같네요"
        percentage < 60 -> "술기운이 좀 올라오네요?"
        percentage < 80 -> "많이 취하신 것 같아요!"
        else -> "매우 위험한 상태입니다!"
    }
}
