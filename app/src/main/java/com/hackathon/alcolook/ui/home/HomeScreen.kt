package com.hackathon.alcolook.ui.home

import android.Manifest
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.PermissionStatus
import com.google.accompanist.permissions.rememberPermissionState
import com.hackathon.alcolook.*
import com.hackathon.alcolook.ui.theme.*
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
    var savedRecords by remember { mutableStateOf<List<Pair<String, Float>>>(emptyList()) }
    val realTimeDrunkDetectionService = remember { DrunkDetectionService(context) }
    val photoDrunkDetectionService = remember { PhotoDrunkDetectionService(context) }
    val coroutineScope = rememberCoroutineScope()
    var currentMode by remember { mutableStateOf("home") }
    
    when (currentMode) {
        "photo" -> {
            PhotoUploadScreen(
                photoDrunkDetectionService = photoDrunkDetectionService,
                onBackToCamera = { currentMode = "home" },
                onSaveRecord = { level ->
                    val currentTime = java.text.SimpleDateFormat("HH:mm", java.util.Locale.getDefault())
                        .format(java.util.Date())
                    // 같은 시간대에 기록이 있으면 더 높은 수치로 업데이트
                    val existingIndex = savedRecords.indexOfFirst { it.first == currentTime }
                    savedRecords = if (existingIndex >= 0) {
                        val existingLevel = savedRecords[existingIndex].second
                        if (level > existingLevel) {
                            savedRecords.toMutableList().apply { 
                                set(existingIndex, Pair(currentTime, level)) 
                            }
                        } else {
                            savedRecords
                        }
                    } else {
                        savedRecords + Pair(currentTime, level)
                    }
                }
            )
        }
        
        else -> {
            // 홈 화면
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.White)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(16.dp))
                
                // Camera preview area - 네모 박스 안에 카메라 표시
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(1.0f)
                        .padding(horizontal = 8.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFF8F8F8)),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .border(
                                width = 2.dp,
                                color = Color(0xFFE0E0E0),
                                shape = RoundedCornerShape(16.dp)
                            )
                            .padding(2.dp)
                            .border(
                                width = 1.dp,
                                color = Color(0xFFE0E0E0),
                                shape = RoundedCornerShape(14.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        if (isCameraActive && cameraPermissionState.status == PermissionStatus.Granted) {
                            // 카메라 프리뷰를 네모 박스 안에 표시
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(Color.Black, RoundedCornerShape(14.dp))
                            ) {
                                CameraPreview(
                                    onImageCaptured = { bitmap ->
                                        if (!isAnalyzing) {
                                            isAnalyzing = true
                                            coroutineScope.launch {
                                                try {
                                                    val result = realTimeDrunkDetectionService.detectDrunkLevel(bitmap)
                                                    faces = result.faces
                                                    currentDrunkLevel = result.drunkLevel
                                                } finally {
                                                    isAnalyzing = false
                                                }
                                            }
                                        }
                                    },
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .clip(RoundedCornerShape(14.dp))
                                )
                                
                                // 얼굴 박스 오버레이
                                if (faces.isNotEmpty()) {
                                    FaceDetectionOverlay(
                                        faces = faces,
                                        imageWidth = 640,
                                        imageHeight = 480,
                                        displayWidth = with(LocalDensity.current) { 
                                            (LocalConfiguration.current.screenWidthDp - 32).dp.toPx()
                                        },
                                        displayHeight = with(LocalDensity.current) { 
                                            (LocalConfiguration.current.screenWidthDp - 32).dp.toPx()
                                        }
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
                            }
                        } else {
                            // 카메라 비활성 상태 - 기본 아이콘 표시
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Surface(
                                    modifier = Modifier.size(48.dp),
                                    shape = RoundedCornerShape(12.dp),
                                    color = Color(0xFFE0E0E0)
                                ) {
                                    Box(
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = "📷",
                                            fontSize = 20.sp,
                                            color = Color.Gray
                                        )
                                    }
                                }
                                
                                Spacer(modifier = Modifier.height(12.dp))
                                
                                Text(
                                    text = if (isCameraActive) "카메라 권한을 허용해주세요" else "카메라를 얼굴에 맞춰 촬영해주세요",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color.Gray,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(20.dp))
                
                // Camera control buttons
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Camera start/stop button
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
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isCameraActive) Color.Red else Color.Black
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = if (isCameraActive) "⏹️" else "📷",
                                fontSize = 16.sp,
                                color = Color.White
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (isCameraActive) "중지" else "시작",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = Color.White
                            )
                        }
                    }
                    
                    // Save record button
                    Button(
                        onClick = {
                            currentDrunkLevel?.let { level ->
                                val currentTime = java.text.SimpleDateFormat("HH:mm", java.util.Locale.getDefault())
                                    .format(java.util.Date())
                                // 같은 시간대(분 단위)에 기록이 있으면 더 높은 수치로 업데이트
                                val existingIndex = savedRecords.indexOfFirst { it.first == currentTime }
                                savedRecords = if (existingIndex >= 0) {
                                    val existingLevel = savedRecords[existingIndex].second
                                    if (level > existingLevel) {
                                        savedRecords.toMutableList().apply { 
                                            set(existingIndex, Pair(currentTime, level)) 
                                        }
                                    } else {
                                        savedRecords
                                    }
                                } else {
                                    savedRecords + Pair(currentTime, level)
                                }
                            }
                        },
                        enabled = currentDrunkLevel != null,
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF007AFF),
                            disabledContainerColor = Color(0xFFE5E5EA)
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "💾",
                                fontSize = 16.sp,
                                color = Color.White
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "기록",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = Color.White
                            )
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // Photo upload button
                OutlinedButton(
                    onClick = { currentMode = "photo" },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .padding(horizontal = 8.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = Color.Black
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "📁",
                            fontSize = 18.sp,
                            color = Color.Black
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "사진 업로드하기",
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.Black
                        )
                    }
                }
                
                // Saved records display
                if (savedRecords.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(20.dp))
                    
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFF0F8FF)),
                        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp)
                        ) {
                            Text(
                                text = "📊 오늘의 기록",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.SemiBold,
                                color = Color.Black
                            )
                            
                            Spacer(modifier = Modifier.height(12.dp))
                            
                            savedRecords.takeLast(3).forEach { (time, level) ->
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = time,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = Color.Black
                                    )
                                    Text(
                                        text = "${level.toInt()}%",
                                        style = MaterialTheme.typography.bodySmall,
                                        fontWeight = FontWeight.Bold,
                                        color = when {
                                            level < 30 -> Color(0xFF34C759)
                                            level < 60 -> Color(0xFFFF9500)
                                            else -> Color(0xFFFF3B30)
                                        }
                                    )
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                            }
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(20.dp))
                
                // Tips card
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFF0F8FF)),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "💡",
                                fontSize = 16.sp
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "촬영 팁",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.SemiBold,
                                color = Color.Black
                            )
                        }
                        
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        Column(
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = "• 밝은 곳에서 촬영하세요",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.Black,
                                lineHeight = 16.sp
                            )
                            Text(
                                text = "• 얼굴이 화면 중앙에 오도록 해주세요",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.Black,
                                lineHeight = 16.sp
                            )
                            Text(
                                text = "• 안경이나 마스크는 벗고 촬영하세요",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.Black,
                                lineHeight = 16.sp
                            )
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(100.dp))
            }
        }
    }
}
