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

enum class HomeScreenState {
    MAIN,
    CAMERA_MEASUREMENT,
    PHOTO_UPLOAD
}

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun HomeScreen() {
    var screenState by remember { mutableStateOf(HomeScreenState.MAIN) }
    
    when (screenState) {
        HomeScreenState.MAIN -> MainHomeScreen(
            onCameraMeasurement = { screenState = HomeScreenState.CAMERA_MEASUREMENT },
            onPhotoUpload = { screenState = HomeScreenState.PHOTO_UPLOAD }
        )
        HomeScreenState.CAMERA_MEASUREMENT -> CameraMeasurementScreen(
            onBack = { screenState = HomeScreenState.MAIN }
        )
        HomeScreenState.PHOTO_UPLOAD -> PhotoMeasurementScreen(
            onBack = { screenState = HomeScreenState.MAIN }
        )
    }
}

@Composable
fun MainHomeScreen(
    onCameraMeasurement: () -> Unit,
    onPhotoUpload: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(32.dp))
        
        // 제목
        Text(
            text = "🍺 AlcoLook",
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = "음주 상태 측정 앱",
            style = MaterialTheme.typography.titleMedium,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        
        Spacer(modifier = Modifier.height(40.dp))
        
        // 데이터 수집 방법 설명
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFF8F9FA)),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(
                modifier = Modifier.padding(20.dp)
            ) {
                Text(
                    text = "측정 방법",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // 1. 얼굴 인식 알고리즘
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "1️⃣",
                        fontSize = 20.sp,
                        modifier = Modifier.width(32.dp)
                    )
                    Column {
                        Text(
                            text = "얼굴 인식 알고리즘",
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "눈, 입, 표정 분석을 통한 음주 상태 판단",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(12.dp))
                
                // 2. 심박수 측정 (선택)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "2️⃣",
                        fontSize = 20.sp,
                        modifier = Modifier.width(32.dp)
                    )
                    Column {
                        Text(
                            text = "심박수 측정 (선택)",
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "카메라 플래시를 이용한 심박수 변화 감지",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(12.dp))
                
                // 3. 자이로센서 측정
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "3️⃣",
                        fontSize = 20.sp,
                        modifier = Modifier.width(32.dp)
                    )
                    Column {
                        Text(
                            text = "자이로센서 측정",
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "기기 흔들림을 통한 균형감각 측정",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
        
        Spacer(modifier = Modifier.height(32.dp))
        
        // 측정 버튼들
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // 카메라로 측정하기 버튼
            Button(
                onClick = onCameraMeasurement,
                modifier = Modifier
                    .weight(1f)
                    .height(120.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                ),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "📷",
                        fontSize = 32.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "카메라로\n측정하기",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        lineHeight = 20.sp
                    )
                }
            }
            
            // 사진 업로드하기 버튼
            Button(
                onClick = onPhotoUpload,
                modifier = Modifier
                    .weight(1f)
                    .height(120.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.secondary
                ),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "📁",
                        fontSize = 32.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "사진\n업로드하기",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        lineHeight = 20.sp
                    )
                }
            }
        }
        
        Spacer(modifier = Modifier.weight(1f))
        
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
}

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun CameraMeasurementScreen(
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val cameraPermissionState = rememberPermissionState(Manifest.permission.CAMERA)
    var faces by remember { mutableStateOf<List<FaceBox>>(emptyList()) }
    var isAnalyzing by remember { mutableStateOf(false) }
    var isCameraActive by remember { mutableStateOf(false) }
    var currentDrunkLevel by remember { mutableStateOf<Float?>(null) }
    val drunkDetectionService = remember { DrunkDetectionService(context) }
    val coroutineScope = rememberCoroutineScope()
    var showAnalysisResult by remember { mutableStateOf(false) }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // 상단 바
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            TextButton(onClick = onBack) {
                Text("← 뒤로")
            }
            Spacer(modifier = Modifier.weight(1f))
            Text(
                text = "카메라 측정",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.weight(1f))
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
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
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // 측정하기 버튼
        Button(
            onClick = { 
                if (isCameraActive) {
                    // 다음 화면으로 이동 (여기서는 분석 결과 화면)
                    showAnalysisResult = true
                } else if (cameraPermissionState.status == PermissionStatus.Granted) {
                    isCameraActive = true
                } else {
                    cameraPermissionState.launchPermissionRequest()
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary
            )
        ) {
            Text(
                text = if (isCameraActive) "다음으로" else "측정하기",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }
        
        Spacer(modifier = Modifier.weight(1f))
    }
    
    // 분석 결과 화면 표시
    if (showAnalysisResult) {
        AnalysisResultScreen(
            drunkLevel = currentDrunkLevel ?: 45f,
            onBack = { 
                showAnalysisResult = false
                onBack()
            }
        )
    }
}

@Composable
fun PhotoMeasurementScreen(
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val photoDrunkDetectionService = remember { PhotoDrunkDetectionService(context) }
    var showPhotoUpload by remember { mutableStateOf(false) }
    var showAnalysisResult by remember { mutableStateOf(false) }
    var analysisResult by remember { mutableStateOf<Float?>(null) }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // 상단 바
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            TextButton(onClick = onBack) {
                Text("← 뒤로")
            }
            Spacer(modifier = Modifier.weight(1f))
            Text(
                text = "사진 업로드",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.weight(1f))
        }
        
        Spacer(modifier = Modifier.height(32.dp))
        
        // 사진 선택 영역
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
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "📁",
                        fontSize = 64.sp,
                        color = Color.Gray
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Text(
                        text = "사진을 선택해주세요",
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.Gray,
                        textAlign = TextAlign.Center
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Text(
                        text = "얼굴이 선명하게 보이는 사진을 선택하세요",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // 사진 선택 버튼
        Button(
            onClick = { showPhotoUpload = true },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.secondary
            )
        ) {
            Text(
                text = "사진 선택하기",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // 다음으로 버튼 (사진 선택 후 활성화)
        Button(
            onClick = { showAnalysisResult = true },
            enabled = analysisResult != null,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary
            )
        ) {
            Text(
                text = "다음으로",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }
        
        Spacer(modifier = Modifier.weight(1f))
    }
    
    // 사진 업로드 화면 표시
    if (showPhotoUpload) {
        PhotoUploadScreen(
            photoDrunkDetectionService = photoDrunkDetectionService,
            onBackToCamera = { showPhotoUpload = false },
            onSaveRecord = { result ->
                analysisResult = result
                showPhotoUpload = false
            }
        )
    }
    
    // 분석 결과 화면 표시
    if (showAnalysisResult) {
        AnalysisResultScreen(
            drunkLevel = analysisResult ?: 45f,
            onBack = { 
                showAnalysisResult = false
                onBack()
            }
        )
    }
}
