package com.hackathon.alcolook

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch

@Composable
fun PhotoUploadScreen(
    photoDrunkDetectionService: PhotoDrunkDetectionService,
    onBackToCamera: () -> Unit,
    onSaveRecord: (Float) -> Unit = {}
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    var selectedBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var faces by remember { mutableStateOf<List<FaceBox>>(emptyList()) }
    var isAnalyzing by remember { mutableStateOf(false) }
    var analysisResult by remember { mutableStateOf<DrunkDetectionResult?>(null) }
    var selectedFaceIndex by remember { mutableStateOf<Int?>(null) }
    
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        selectedImageUri = uri
        uri?.let {
            try {
                val inputStream = context.contentResolver.openInputStream(it)
                val bitmap = BitmapFactory.decodeStream(inputStream)
                selectedBitmap = bitmap
                faces = emptyList()
                analysisResult = null
                selectedFaceIndex = null
                faces = emptyList() // 새 이미지 선택 시 결과 초기화
            } catch (e: Exception) {
                // 이미지 로드 실패
            }
        }
    }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // 상단 제목
        Text(
            text = "사진 업로드 모드",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(vertical = 16.dp)
        )
        
        // 이미지 표시 영역
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            selectedBitmap?.let { bitmap ->
                Image(
                    bitmap = bitmap.asImageBitmap(),
                    contentDescription = "선택된 이미지",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Fit
                )
                
                // 얼굴 박스 오버레이
                if (faces.isNotEmpty()) {
                    FaceDetectionOverlay(
                        faces = faces,
                        imageWidth = bitmap.width,
                        imageHeight = bitmap.height,
                        displayWidth = with(LocalDensity.current) { 
                            LocalConfiguration.current.screenWidthDp.dp.toPx() - 32.dp.toPx()
                        },
                        displayHeight = with(LocalDensity.current) { 
                            (LocalConfiguration.current.screenHeightDp * 0.6f).dp.toPx()
                        }
                    )
                }
            } ?: run {
                // 이미지가 없을 때 플레이스홀더
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "📷",
                        fontSize = 64.sp
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "사진을 선택해주세요",
                        fontSize = 18.sp,
                        color = Color.Gray
                    )
                }
            }
        }
        
        // 버튼 영역
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // 사진 선택 버튼
            Button(
                onClick = { imagePickerLauncher.launch("image/*") },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF007AFF))
            ) {
                Text("사진 선택", fontSize = 16.sp)
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // 분석 버튼
            selectedBitmap?.let { bitmap ->
                Button(
                    onClick = {
                        isAnalyzing = true
                        coroutineScope.launch {
                            try {
                                val result = photoDrunkDetectionService.detectDrunkLevel(bitmap)
                                faces = result.faces
                                analysisResult = result
                                selectedFaceIndex = if (result.faces.isNotEmpty()) 0 else null
                            } finally {
                                isAnalyzing = false
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !isAnalyzing,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF34C759))
                ) {
                    if (isAnalyzing) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("분석 중...")
                    } else {
                        Text("음주도 분석", fontSize = 16.sp)
                    }
                }
                
                Spacer(modifier = Modifier.height(12.dp))
            }
            
            // 다중 얼굴 선택 UI
            if (faces.size > 1) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFF0F8FF))
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            text = "👥 ${faces.size}명이 감지되었습니다. 기록할 사람을 선택하세요:",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        faces.forEachIndexed { index, face ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(
                                    selected = selectedFaceIndex == index,
                                    onClick = { selectedFaceIndex = index }
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "${face.personId}: ${face.drunkPercentage}%",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = when {
                                        face.drunkPercentage < 30 -> Color(0xFF34C759)
                                        face.drunkPercentage < 60 -> Color(0xFFFF9500)
                                        else -> Color(0xFFFF3B30)
                                    }
                                )
                            }
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(12.dp))
            }
            
            // 분석 결과 및 기록 버튼
            analysisResult?.let { result ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFF8F8F8))
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            text = "📊 분석 결과",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        val selectedFace = selectedFaceIndex?.let { faces.getOrNull(it) }
                        val displayLevel = selectedFace?.drunkPercentage?.toFloat() ?: result.drunkLevel
                        
                        Text(
                            text = if (faces.size > 1 && selectedFace != null) {
                                "${selectedFace.personId}: ${selectedFace.drunkPercentage}%"
                            } else {
                                "음주도: ${displayLevel.toInt()}%"
                            },
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = when {
                                displayLevel < 30 -> Color(0xFF34C759)
                                displayLevel < 60 -> Color(0xFFFF9500)
                                else -> Color(0xFFFF3B30)
                            }
                        )
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        Text(
                            text = result.message,
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.Gray
                        )
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        // 기록 버튼
                        Button(
                            onClick = {
                                onSaveRecord(displayLevel)
                            },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF007AFF))
                        ) {
                            Text(
                                text = "💾 기록하기",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(12.dp))
            }
            
            // 실시간 모드로 돌아가기 버튼
            OutlinedButton(
                onClick = onBackToCamera,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("실시간 카메라 모드로 돌아가기")
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
