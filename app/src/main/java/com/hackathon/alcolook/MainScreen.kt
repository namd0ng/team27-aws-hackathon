package com.hackathon.alcolook

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun MainScreen(modifier: Modifier = Modifier) {
    var currentScreen by remember { mutableStateOf(Screen.MAIN) }
    
    when (currentScreen) {
        Screen.MAIN -> {
            Column(
                modifier = modifier
                    .fillMaxSize()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "AlcoLook",
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 48.dp)
                )
                
                Text(
                    text = "간접 음주 측정 시스템",
                    fontSize = 18.sp,
                    modifier = Modifier.padding(bottom = 32.dp)
                )
                
                Button(
                    onClick = { currentScreen = Screen.HEART_RATE },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                ) {
                    Text("1단계: 심박수 측정", fontSize = 16.sp)
                }
                
                Button(
                    onClick = { currentScreen = Screen.WALKING_TEST },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                ) {
                    Text("2단계: 보행 테스트", fontSize = 16.sp)
                }
            }
        }
        Screen.HEART_RATE -> {
            HeartRateScreen(
                modifier = modifier,
                onBack = { currentScreen = Screen.MAIN }
            )
        }
        Screen.WALKING_TEST -> {
            WalkingTestScreen(
                modifier = modifier,
                onBack = { currentScreen = Screen.MAIN }
            )
        }
    }
}

enum class Screen {
    MAIN, HEART_RATE, WALKING_TEST
}
