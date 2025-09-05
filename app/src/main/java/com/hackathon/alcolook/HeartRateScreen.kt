package com.hackathon.alcolook

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.health.connect.client.PermissionController
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlin.math.abs

@Composable
fun HeartRateScreen(
    modifier: Modifier = Modifier,
    onBack: (() -> Unit)? = null,
    viewModel: HeartRateViewModel? = null
) {
    val context = LocalContext.current
    val heartRateViewModel = viewModel ?: viewModel { HeartRateViewModel(context) }
    val uiState by heartRateViewModel.uiState.collectAsState()
    
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = PermissionController.createRequestPermissionResultContract()
    ) { granted ->
        if (granted.containsAll(HealthConnectManager(context).permissions)) {
            heartRateViewModel.checkPermissions()
        }
    }
    
    LaunchedEffect(Unit) {
        heartRateViewModel.checkPermissions()
    }
    
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "심박수 기반 음주 측정",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 32.dp)
        )
        
        onBack?.let {
            Button(
                onClick = it,
                modifier = Modifier.padding(bottom = 16.dp)
            ) {
                Text("← 메인으로")
            }
        }
        
        if (!uiState.hasPermissions) {
            Text(
                text = "Health Connect 권한이 필요합니다",
                modifier = Modifier.padding(bottom = 16.dp)
            )
            Button(
                onClick = {
                    permissionLauncher.launch(HealthConnectManager(context).permissions)
                }
            ) {
                Text("권한 요청")
            }
        } else {
            if (uiState.isLoading) {
                CircularProgressIndicator()
                Text(
                    text = "심박수 측정 중...",
                    modifier = Modifier.padding(top = 16.dp)
                )
            } else {
                uiState.heartRateData?.let { data ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "현재 심박수",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                text = "${data.bpm} BPM",
                                fontSize = 32.sp,
                                fontWeight = FontWeight.Bold,
                                color = getAlcoholLevelColor(uiState.alcoholLevel)
                            )
                            
                            uiState.baselineHeartRate?.let { baseline ->
                                Text(
                                    text = "평균 심박수: ${baseline.toInt()} BPM",
                                    fontSize = 14.sp,
                                    color = Color.Gray,
                                    modifier = Modifier.padding(top = 8.dp)
                                )
                                
                                val difference = abs(data.bpm - baseline)
                                Text(
                                    text = "차이: ${difference.toInt()} BPM",
                                    fontSize = 14.sp,
                                    color = Color.Gray
                                )
                            }
                            
                            Spacer(modifier = Modifier.height(16.dp))
                            
                            Text(
                                text = "음주 수준: ${getAlcoholLevelText(uiState.alcoholLevel)}",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = getAlcoholLevelColor(uiState.alcoholLevel)
                            )
                        }
                    }
                }
                
                if (uiState.baselineHeartRate == null && !uiState.isLoading) {
                    Text(
                        text = "베이스라인 데이터가 부족합니다\n일주일간 데이터를 수집해주세요",
                        modifier = Modifier.padding(16.dp),
                        color = Color.Gray
                    )
                }
                
                // 나이 입력
                var ageText by remember { mutableStateOf("30") }
                OutlinedTextField(
                    value = ageText,
                    onValueChange = { ageText = it },
                    label = { Text("나이") },
                    modifier = Modifier.padding(vertical = 8.dp)
                )
                
                Button(
                    onClick = { 
                        val age = ageText.toIntOrNull() ?: 30
                        heartRateViewModel.measureHeartRate(age) 
                    },
                    modifier = Modifier.padding(top = 16.dp)
                ) {
                    Text("심박수 측정")
                }
            }
        }
    }
}
