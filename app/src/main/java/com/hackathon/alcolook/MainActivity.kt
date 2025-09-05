package com.hackathon.alcolook

import android.Manifest
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.health.connect.client.PermissionController
import androidx.lifecycle.lifecycleScope
import com.hackathon.alcolook.ui.theme.AlcoLookTheme
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay

class MainActivity : ComponentActivity() {
    
    private lateinit var permissionHandler: PermissionHandler
    private var hasPermissions by mutableStateOf(false)
    
    // Health Connect 권한 요청 처리
    private val permissionLauncher = registerForActivityResult(
        PermissionController.createRequestPermissionResultContract()
    ) { granted ->
        Log.d("HEALTH_CONNECT", "권한 요청 결과: $granted")
        Log.d("HEALTH_CONNECT", "허용된 권한 개수: ${granted.size}")
        
        lifecycleScope.launch {
            try {
                // 권한 상태 재확인
                kotlinx.coroutines.delay(1000) // Health Connect 동기화 대기
                hasPermissions = permissionHandler.hasHealthPermissions()
                Log.d("HEALTH_CONNECT", "권한 재확인 결과: $hasPermissions")
                
                if (!hasPermissions && granted.isNotEmpty()) {
                    // 권한이 허용되었지만 아직 반영되지 않은 경우
                    Log.d("HEALTH_CONNECT", "권한 동기화 대기 중...")
                    kotlinx.coroutines.delay(2000)
                    hasPermissions = permissionHandler.hasHealthPermissions()
                    Log.d("HEALTH_CONNECT", "최종 권한 상태: $hasPermissions")
                }
            } catch (e: Exception) {
                Log.e("HEALTH_CONNECT", "권한 상태 확인 중 오류", e)
            }
        }
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        Log.d("HEALTH_CONNECT", "앱 실행됨")
        permissionHandler = PermissionHandler(this)
        
        setContent {
            AlcoLookTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    if (hasPermissions) {
                        TestFlowScreen(
                            modifier = Modifier.padding(innerPadding)
                        )
                    } else {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(innerPadding),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            CircularProgressIndicator()
                            Text(
                                text = "Health Connect 권한 확인 중...",
                                modifier = Modifier.padding(top = 16.dp)
                            )
                        }
                    }
                }
            }
        }
        
        // UI 설정 후 권한 요청
        lifecycleScope.launch {
            try {
                hasPermissions = permissionHandler.hasHealthPermissions()
                Log.d("HEALTH_CONNECT", "초기 권한 상태: $hasPermissions")
                
                if (!hasPermissions) {
                    Log.d("HEALTH_CONNECT", "권한이 없음, 권한 요청 시작")
                    // 잠시 대기 후 권한 요청 (UI가 완전히 로드된 후)
                    kotlinx.coroutines.delay(500)
                    permissionLauncher.launch(permissionHandler.healthPermissions)
                }
            } catch (e: Exception) {
                Log.e("HEALTH_CONNECT", "권한 확인 중 오류", e)
                // 오류가 발생해도 권한 요청 시도
                try {
                    permissionLauncher.launch(permissionHandler.healthPermissions)
                } catch (e2: Exception) {
                    Log.e("HEALTH_CONNECT", "권한 요청도 실패", e2)
              Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    AlcoLookNavigation()
                }
            }
        }
    }
}

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun DrunkDetectionScreen() {
    val context = LocalContext.current
    val cameraPermissionState = rememberPermissionState(Manifest.permission.CAMERA)
    var faces by remember { mutableStateOf<List<FaceBox>>(emptyList()) }
    var isAnalyzing by remember { mutableStateOf(false) }
    var currentMode by remember { mutableStateOf("camera") } // "camera" or "photo"
    val realTimeDrunkDetectionService = remember { DrunkDetectionService(context) }
    val photoDrunkDetectionService = remember { PhotoDrunkDetectionService(context) }
    val coroutineScope = rememberCoroutineScope()
    
    if (cameraPermissionState.status == PermissionStatus.Granted && currentMode == "camera") {
        // 실시간 카메라 모드
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black)
        ) {
            CameraPreview(
                onImageCaptured = { bitmap ->
                    if (!isAnalyzing) {
                        isAnalyzing = true
                        coroutineScope.launch {
                            try {
                                val result = realTimeDrunkDetectionService.detectDrunkLevel(bitmap)
                                faces = result.faces
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
                    imageWidth = 640, // 카메라 해상도에 맞게 조정
                    imageHeight = 480,
                    displayWidth = with(LocalDensity.current) { 
                        LocalConfiguration.current.screenWidthDp.dp.toPx() 
                    },
                    displayHeight = with(LocalDensity.current) { 
                        LocalConfiguration.current.screenHeightDp.dp.toPx() 
                    }
                )
            }
            
            // 하단 모드 전환 버튼
            Button(
                onClick = { currentMode = "photo" },
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(32.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color.Blue.copy(alpha = 0.8f))
            ) {
                Text("사진 업로드 모드", color = Color.White)
            }
        }
    } else if (currentMode == "photo") {
        // 사진 업로드 모드
        PhotoUploadScreen(
            photoDrunkDetectionService = photoDrunkDetectionService,
            onBackToCamera = { currentMode = "camera" }
        )
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
                text = "AlcoLook",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = "음주 감지 앱",
                fontSize = 16.sp,
                textAlign = TextAlign.Center,
                color = Color.Gray
            )
            
            Spacer(modifier = Modifier.height(48.dp))
            
            Button(
                onClick = { cameraPermissionState.launchPermissionRequest() },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("실시간 카메라 모드 시작")
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Button(
                onClick = { currentMode = "photo" },
                colors = ButtonDefaults.buttonColors(containerColor = Color.Blue),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("사진 업로드 모드 시작")
            }
            
            Spacer(modifier = Modifier.height(32.dp))
            
            Text(
                text = "실시간 모드는 카메라 권한이 필요합니다",
                fontSize = 14.sp,
                textAlign = TextAlign.Center,
                color = Color.Gray
            )
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
