package com.hackathon.alcolook

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hackathon.alcolook.ui.theme.AlcoLookTheme

class PermissionsRationaleActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AlcoLookTheme {
                PermissionsRationaleScreen(
                    onClose = { finish() }
                )
            }
        }
    }
}

@Composable
fun PermissionsRationaleScreen(onClose: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "개인정보처리방침",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 24.dp)
        )
        
        Text(
            text = "AlcoLook은 다음과 같은 목적으로 건강 데이터를 사용합니다:",
            fontSize = 16.sp,
            modifier = Modifier.padding(bottom = 16.dp)
        )
        
        Text(
            text = "• 심박수 데이터: 음주 상태 간접 측정\n" +
                  "• 운동 세션 데이터: 정확한 측정을 위한 운동 중 데이터 제외\n" +
                  "• 모든 데이터는 기기 내에서만 처리되며 외부로 전송되지 않습니다",
            fontSize = 14.sp,
            modifier = Modifier.padding(bottom = 32.dp)
        )
        
        Button(
            onClick = onClose,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("확인")
        }
    }
}
