package com.hackathon.alcolook

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
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
                }
            }
        }
    }
}
