package com.hackathon.alcolook.ui.home

import android.Manifest
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
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
import com.hackathon.alcolook.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun HomeScreen() {
    val context = LocalContext.current
    val cameraPermissionState = rememberPermissionState(Manifest.permission.CAMERA)
    var faces by remember { mutableStateOf<List<FaceBox>>(emptyList()) }
    var isAnalyzing by remember { mutableStateOf(false) }
    var isCameraActive by remember { mutableStateOf(false) }
    var currentDrunkLevel by remember { mutableStateOf<Float?>(null) }
    val drunkDetectionService = remember { DrunkDetectionService(context) }
    val photoDrunkDetectionService = remember { PhotoDrunkDetectionService(context) }
    var showPhotoUpload by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()
    var showAnalysisResult by remember { mutableStateOf(false) }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(16.dp))
        
        // 제목
        Text(
            text = "🍺 AlcoLook",
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = "얼굴 인식 기반 음주 상태 분석",
            style = MaterialTheme.typography.titleMedium,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // 카메라 프리뷰 영역
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1.0f),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFF8F8F8)),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                if (isCameraActive && cameraPermissionState.status == PermissionStatus.Granted) {
                    // 카메라 프리뷰
                    CameraPreview(
                        onImageCaptured = { bitmap ->
                            if (!isAnalyzing) {
                                isAnalyzing = true
                                coroutineScope.launch {
                                    try {
                                        val result = drunkDetectionService.detectDrunkLevel(bitmap)
                                        faces = result.faces
                                        currentDrunkLevel = result.drunkLevel
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
                            displayWidth = 300f,
                            displayHeight = 300f
                        )
                    }
                    
                    // 현재 음주도 표시
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
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "📷",
                            fontSize = 48.sp,
                            color = Color.Gray
                        )
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        Text(
                            text = if (cameraPermissionState.status == PermissionStatus.Granted) 
                                "카메라를 시작하려면 아래 버튼을 눌러주세요" 
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
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // 컨트롤 버튼들
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // 카메라 시작/중지 버튼
            Button(
                onClick = { 
                    if (isCameraActive) {
                        isCameraActive = false
                        currentDrunkLevel = null
                        faces = emptyList()
                    } else if (cameraPermissionState.status == PermissionStatus.Granted) {
                        isCameraActive = true
                    } else {
                        cameraPermissionState.launchPermissionRequest()
                    }
                },
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isCameraActive) Color.Red else MaterialTheme.colorScheme.primary
                )
            ) {
                Text(
                    text = if (isCameraActive) "⏹️ 중지" else "📷 시작",
                    fontWeight = FontWeight.SemiBold
                )
            }
            
            // 파일 업로드 버튼
            Button(
                onClick = { showPhotoUpload = true },
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.tertiary
                )
            ) {
                Text(
                    text = "📁 사진 분석",
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
        
        Spacer(modifier = Modifier.height(12.dp))
        
        // 분석 결과 버튼
        Button(
            onClick = { showAnalysisResult = true },
            enabled = true,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.secondary
            )
        ) {
            Text(
                text = "📊 상세 분석 결과",
                fontWeight = FontWeight.SemiBold
            )
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // 현재 상태 표시
        currentDrunkLevel?.let { level ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = when {
                        level < 20 -> Color(0xFFE8F5E8)
                        level < 40 -> Color(0xFFFFF4E5)
                        level < 60 -> Color(0xFFFFE0B2)
                        else -> Color(0xFFFDEBEC)
                    }
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "현재 음주 상태",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Text(
                        text = "${level.toInt()}%",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = when {
                            level < 20 -> Color(0xFF2E7D32)
                            level < 40 -> Color(0xFFE65100)
                            level < 60 -> Color(0xFFBF360C)
                            else -> Color(0xFFC62828)
                        }
                    )
                    
                    Text(
                        text = when {
                            level < 20 -> "정상 상태"
                            level < 40 -> "약간 취한 상태"
                            level < 60 -> "취한 상태"
                            else -> "매우 취한 상태"
                        },
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // 면책 고지
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF3E0))
        ) {
            Column(
                modifier = Modifier.padding(12.dp)
            ) {
                Text(
                    text = "⚠️ 면책 고지",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFE65100)
                )
                
                Spacer(modifier = Modifier.height(4.dp))
                
                Text(
                    text = "본 분석 결과는 의료 목적이 아니며, 운전 가능 여부를 판단하는 기준으로 사용할 수 없습니다.",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFFBF360C),
                    lineHeight = 16.sp
                )
            }
        }
    }
    
    // 화면 전환 로직
    if (showAnalysisResult) {
        AnalysisResultScreen(
            drunkLevel = currentDrunkLevel ?: 45f,
            onBack = { showAnalysisResult = false }
        )
    } else if (showPhotoUpload) {
        PhotoUploadScreen(
            photoDrunkDetectionService = photoDrunkDetectionService,
            onBackToCamera = { showPhotoUpload = false },
            onSaveRecord = { result ->
                currentDrunkLevel = result
                showPhotoUpload = false
                showAnalysisResult = true
            }
        )
    }
}
