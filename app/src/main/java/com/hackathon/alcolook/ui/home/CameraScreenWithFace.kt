package com.hackathon.alcolook.ui.home

import android.Manifest
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.PermissionStatus
import com.google.accompanist.permissions.rememberPermissionState
import com.hackathon.alcolook.CameraPreview
import com.hackathon.alcolook.DrunkDetectionService
import com.hackathon.alcolook.FaceBox
import com.hackathon.alcolook.FaceDetectionOverlay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class, ExperimentalPermissionsApi::class)
@Composable
fun CameraScreenWithFace(
    onMeasureClick: (Float) -> Unit,
    onBackClick: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val cameraPermissionState = rememberPermissionState(Manifest.permission.CAMERA)
    val drunkDetectionService = remember { DrunkDetectionService(context) }
    
    var faces by remember { mutableStateOf<List<FaceBox>>(emptyList()) }
    var isAnalyzing by remember { mutableStateOf(false) }
    var isCameraActive by remember { mutableStateOf(false) }
    var currentDrunkLevel by remember { mutableStateOf<Float?>(null) }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(20.dp))
        
        // 제목
        Text(
            text = "얼굴 인식 측정",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // 카메라 프리뷰 영역 (높이 줄임)
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(300.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Box(
                modifier = Modifier.fillMaxSize()
            ) {
                if (isCameraActive && cameraPermissionState.status == PermissionStatus.Granted) {
                    // 카메라 프리뷰
                    CameraPreview(
                        onImageCaptured = { bitmap ->
                            if (!isAnalyzing) {
                                isAnalyzing = true
                                scope.launch {
                                    try {
                                        val result = drunkDetectionService.detectDrunkLevel(bitmap)
                                        faces = result.faceBoxes
                                        currentDrunkLevel = result.drunkPercentage
                                    } finally {
                                        isAnalyzing = false
                                    }
                                }
                            }
                        },
                        modifier = Modifier.fillMaxSize()
                    )
                    
                    // 얼굴 박스 오버레이
                    if (faces.isNotEmpty()) {
                        FaceDetectionOverlay(
                            faces = faces,
                            imageWidth = 640,
                            imageHeight = 480,
                            displayWidth = 400f,
                            displayHeight = 400f,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                    
                    // 음주 확률 표시 (이미 퍼센트 값이므로 그대로 사용)
                    currentDrunkLevel?.let { level ->
                        Card(
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .padding(8.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = Color.Black.copy(alpha = 0.7f)
                            )
                        ) {
                            Text(
                                text = "${level.toInt()}%",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(8.dp)
                            )
                        }
                    }
                } else {
                    // 카메라 비활성 상태
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "📷",
                            fontSize = 48.sp,
                            color = Color.Gray
                        )
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        Text(
                            text = if (cameraPermissionState.status == PermissionStatus.Granted) 
                                "측정하기 버튼을 눌러 시작하세요" 
                            else 
                                "카메라 권한을 허용해주세요",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.Gray,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // 측정 가이드
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "📋 측정 가이드",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "• 얼굴을 화면 중앙에 위치시켜주세요\n• 조명이 밝은 곳에서 측정해주세요\n• 카메라를 정면으로 바라봐주세요",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurface,
                    lineHeight = 20.sp
                )
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // 버튼들
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 뒤로가기 버튼
            OutlinedButton(
                onClick = onBackClick,
                modifier = Modifier.weight(1f)
            ) {
                Text("뒤로가기")
            }
            
            // 측정하기/기록하기 버튼
            Button(
                onClick = { 
                    if (isCameraActive && currentDrunkLevel != null) {
                        // 얼굴 인식 완료 시 다음 단계로
                        onMeasureClick(currentDrunkLevel!!)
                    } else if (cameraPermissionState.status == PermissionStatus.Granted) {
                        // 카메라 시작
                        isCameraActive = true
                    } else {
                        // 권한 요청
                        cameraPermissionState.launchPermissionRequest()
                    }
                },
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = when {
                        isCameraActive && currentDrunkLevel != null -> "기록하기"
                        isCameraActive -> "측정 중..."
                        else -> "측정하기"
                    }
                )
            }
        }
        
        Spacer(modifier = Modifier.height(12.dp))
        
        // 면책 고지
        Text(
            text = "⚠️ 본 측정 결과는 의료 목적이 아니며, 운전 판단에 사용하지 마세요.",
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(20.dp))
    }
}
