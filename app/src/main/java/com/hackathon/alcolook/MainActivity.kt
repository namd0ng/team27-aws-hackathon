package com.hackathon.alcolook

import android.Manifest
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.health.connect.client.PermissionController
import androidx.lifecycle.lifecycleScope
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.PermissionStatus
import com.google.accompanist.permissions.rememberPermissionState
import com.hackathon.alcolook.ui.theme.AlcoLookTheme
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay

class MainActivity : ComponentActivity() {
    
    private lateinit var permissionHandler: PermissionHandler
    private var hasPermissions by mutableStateOf(false)
    private var isCheckingPermissions by mutableStateOf(true)
    
    private val cameraPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            Log.d("MainActivity", "카메라 권한 승인됨")
            checkHealthPermissions()
        } else {
            Log.d("MainActivity", "카메라 권한 거부됨")
            isCheckingPermissions = false
        }
    }
    
    private val healthPermissionLauncher = registerForActivityResult(
        PermissionController.createRequestPermissionResultContract()
    ) { granted ->
        Log.d("MainActivity", "Health Connect 권한 결과: $granted")
        hasPermissions = granted.containsAll(permissionHandler.healthPermissions)
        isCheckingPermissions = false
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        permissionHandler = PermissionHandler(this)
        
        setContent {
            AlcoLookTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    when {
                        isCheckingPermissions -> {
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
                        hasPermissions -> {
                            MainScreen(modifier = Modifier.padding(innerPadding))
                        }
                        else -> {
                            PermissionScreen(
                                onRequestPermissions = { requestPermissions() },
                                modifier = Modifier.padding(innerPadding)
                            )
                        }
                    }
                }
            }
        }
        
        // 권한 확인 시작
        checkPermissions()
    }
    
    private fun checkPermissions() {
        lifecycleScope.launch {
            delay(1000) // UI 안정화를 위한 지연
            
            if (!permissionHandler.isHealthConnectAvailable()) {
                Log.e("MainActivity", "Health Connect를 사용할 수 없습니다")
                isCheckingPermissions = false
                return@launch
            }
            
            val hasHealthPermissions = permissionHandler.hasHealthPermissions()
            Log.d("MainActivity", "Health Connect 권한 상태: $hasHealthPermissions")
            
            hasPermissions = hasHealthPermissions
            isCheckingPermissions = false
        }
    }
    
    private fun checkHealthPermissions() {
        lifecycleScope.launch {
            val hasHealthPermissions = permissionHandler.hasHealthPermissions()
            if (!hasHealthPermissions) {
                healthPermissionLauncher.launch(permissionHandler.healthPermissions)
            } else {
                hasPermissions = true
                isCheckingPermissions = false
            }
        }
    }
    
    private fun requestPermissions() {
        cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
    }
}

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun MainScreen(modifier: Modifier = Modifier) {
    val cameraPermissionState = rememberPermissionState(Manifest.permission.CAMERA)
    
    when (cameraPermissionState.status) {
        is PermissionStatus.Granted -> {
            TestFlowScreen(modifier = modifier)
        }
        is PermissionStatus.Denied -> {
            PermissionScreen(
                onRequestPermissions = { cameraPermissionState.launchPermissionRequest() },
                modifier = modifier
            )
        }
    }
}

@Composable
fun PermissionScreen(
    onRequestPermissions: () -> Unit,
    modifier: Modifier = Modifier
) {
    val configuration = LocalConfiguration.current
    val density = LocalDensity.current
    
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // 앱 아이콘 영역
        Box(
            modifier = Modifier
                .size(120.dp)
                .clip(CircleShape)
                .background(Color(0xFF1976D2)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "🍺",
                fontSize = 48.sp
            )
        }
        
        Spacer(modifier = Modifier.height(32.dp))
        
        Text(
            text = "AlcoLook",
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF1976D2)
        )
        
        Text(
            text = "음주 상태 측정 앱",
            fontSize = 18.sp,
            color = Color.Gray,
            modifier = Modifier.padding(top = 8.dp, bottom = 32.dp)
        )
        
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 32.dp)
        ) {
            Column(
                modifier = Modifier.padding(20.dp)
            ) {
                Text(
                    text = "필요한 권한",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
                
                PermissionItem(
                    icon = "📷",
                    title = "카메라",
                    description = "얼굴 분석을 위한 사진 촬영"
                )
                
                PermissionItem(
                    icon = "💓",
                    title = "Health Connect",
                    description = "심박수 데이터 읽기"
                )
                
                PermissionItem(
                    icon = "📱",
                    title = "센서",
                    description = "보행 분석을 위한 자이로스코프"
                )
            }
        }
        
        Button(
            onClick = onRequestPermissions,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
        ) {
            Text(
                text = "권한 허용하기",
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium
            )
        }
        
        Text(
            text = "모든 데이터는 기기에만 저장되며 외부로 전송되지 않습니다",
            fontSize = 12.sp,
            color = Color.Gray,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 16.dp)
        )
    }
}

@Composable
private fun PermissionItem(
    icon: String,
    title: String,
    description: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = icon,
            fontSize = 24.sp,
            modifier = Modifier.padding(end = 12.dp)
        )
        Column {
            Text(
                text = title,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = description,
                fontSize = 12.sp,
                color = Color.Gray,
                modifier = Modifier.padding(start = 12.dp, top = 2.dp)
            )
        }
    }
}
