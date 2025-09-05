package com.hackathon.alcolook

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.launch

@Composable
fun HeartRateScreen(
    modifier: Modifier = Modifier,
    viewModel: HeartRateViewModel = viewModel()
) {
    val context = LocalContext.current
    val permissionHandler = remember { PermissionHandler(context) }
    val scope = rememberCoroutineScope()
    
    var heartRateData by remember { mutableStateOf<HeartRateData?>(null) }
    var isLoading by remember { mutableStateOf(false) }
    
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "심박수 측정",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 32.dp)
        )
        
        if (isLoading) {
            CircularProgressIndicator()
            Text(
                text = "측정 중...",
                modifier = Modifier.padding(top = 16.dp)
            )
        } else {
            heartRateData?.let { data ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "심박수",
                            fontSize = 16.sp
                        )
                        Text(
                            text = "${data.bpm} BPM",
                            fontSize = 32.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
            
            Button(
                onClick = {
                    scope.launch {
                        isLoading = true
                        try {
                            val data = permissionHandler.readRecentHeartRate()
                            heartRateData = data
                        } finally {
                            isLoading = false
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("심박수 측정")
            }
        }
    }
}
