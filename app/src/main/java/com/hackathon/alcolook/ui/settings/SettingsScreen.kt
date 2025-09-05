package com.hackathon.alcolook.ui.settings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Header
        Text(
            text = "설정",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 24.dp)
        )
        
        // Profile Section
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "프로필",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(12.dp))
                
                SettingsItem(
                    title = "성별",
                    subtitle = "설정되지 않음",
                    onClick = { /* TODO: Gender selection */ }
                )
                
                SettingsItem(
                    title = "연령대",
                    subtitle = "설정되지 않음",
                    onClick = { /* TODO: Age selection */ }
                )
                
                SettingsItem(
                    title = "주간 목표",
                    subtitle = "설정되지 않음",
                    onClick = { /* TODO: Weekly goal */ }
                )
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Data Management Section
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "데이터 관리",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(12.dp))
                
                SettingsItem(
                    title = "데이터 전체 삭제",
                    subtitle = "모든 기록을 삭제합니다",
                    onClick = { /* TODO: Data deletion */ },
                    isDestructive = true
                )
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Help Section
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "도움말",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(12.dp))
                
                SettingsItem(
                    title = "앱 정보",
                    subtitle = "버전 1.0",
                    onClick = { /* TODO: App info */ }
                )
                
                SettingsItem(
                    title = "면책 고지",
                    subtitle = "이용 약관 및 주의사항",
                    onClick = { /* TODO: Disclaimer */ }
                )
            }
        }
        
        Spacer(modifier = Modifier.weight(1f))
        
        // Disclaimer
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            ),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text(
                text = "⚠️ 이 앱은 의료 목적이 아니며, 운전 판단에 사용하지 마세요.\n모든 데이터는 로컬에 저장되며 외부로 전송되지 않습니다.",
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(16.dp),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun SettingsItem(
    title: String,
    subtitle: String,
    onClick: () -> Unit,
    isDestructive: Boolean = false
) {
    TextButton(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        contentPadding = PaddingValues(vertical = 12.dp, horizontal = 0.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.Start
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                color = if (isDestructive) MaterialTheme.colorScheme.error 
                       else MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}