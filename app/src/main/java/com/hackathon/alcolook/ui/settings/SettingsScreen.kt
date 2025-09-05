package com.hackathon.alcolook.ui.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hackathon.alcolook.R
import com.hackathon.alcolook.ui.components.DropdownSettingsItem
import com.hackathon.alcolook.ui.components.rememberSettingsState
import com.hackathon.alcolook.ui.theme.*

/**
 * 설정 화면 - 스크린샷과 정확히 일치하는 디자인
 */
@Composable
fun SettingsScreen() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F5F5))
            .verticalScroll(rememberScrollState())
    ) {
        // 헤더
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White)
                .padding(16.dp)
        ) {
            Text(
                text = "설정",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // 개인 정보 섹션
        PersonalInfoSection()
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // 데이터 관리 섹션
        DataManagementSection()
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // 앱 정보 섹션
        AppInfoSection()
        
        Spacer(modifier = Modifier.height(100.dp))
    }
}

@Composable
private fun PersonalInfoSection() {
    val genderState = rememberSettingsState("선택")
    val ageGroupState = rememberSettingsState("선택")
    val weeklyGoalState = rememberSettingsState("선택")
    val themeState = rememberSettingsState("선택")
    
    Column {
        Text(
            text = "개인 정보",
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            style = MaterialTheme.typography.titleMedium,
            color = Color.Gray
        )
        
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column {
                SettingsItem(
                    icon = "👤",
                    title = "성별",
                    value = genderState.value,
                    onClick = { /* TODO */ }
                )
                
                HorizontalDivider(
                    color = Color(0xFFE0E0E0),
                    thickness = 0.5.dp,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
                
                SettingsItem(
                    icon = "🎂",
                    title = "연령대",
                    value = ageGroupState.value,
                    onClick = { /* TODO */ }
                )
                
                HorizontalDivider(
                    color = Color(0xFFE0E0E0),
                    thickness = 0.5.dp,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
                
                SettingsItem(
                    icon = "🏁",
                    title = "주간 목표",
                    value = weeklyGoalState.value,
                    onClick = { /* TODO */ }
                )
                
                HorizontalDivider(
                    color = Color(0xFFE0E0E0),
                    thickness = 0.5.dp,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
                
                SettingsItem(
                    icon = "🎨",
                    title = "테마 설정",
                    value = themeState.value,
                    onClick = { /* TODO */ }
                )
            }
        }
    }
}

@Composable
private fun DataManagementSection() {
    Column {
        Text(
            text = "데이터 관리",
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            style = MaterialTheme.typography.titleMedium,
            color = Color.Gray
        )
        
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column {
                SettingsItem(
                    icon = "💾",
                    title = "데이터 백업",
                    subtitle = "로컬 데이터 내보내기",
                    onClick = { /* TODO */ }
                )
                
                HorizontalDivider(
                    color = Color(0xFFE0E0E0),
                    thickness = 0.5.dp,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
                
                SettingsItem(
                    icon = "🗑️",
                    title = "데이터 전체 삭제",
                    subtitle = "모든 기록 삭제",
                    onClick = { /* TODO */ }
                )
            }
        }
    }
}

@Composable
private fun AppInfoSection() {
    Column {
        Text(
            text = "앱 정보",
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            style = MaterialTheme.typography.titleMedium,
            color = Color.Gray
        )
        
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column {
                SettingsItem(
                    icon = "ℹ️",
                    title = "앱 정보",
                    subtitle = "버전 1.0.0",
                    onClick = { /* TODO */ }
                )
                
                HorizontalDivider(
                    color = Color(0xFFE0E0E0),
                    thickness = 0.5.dp,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
                
                SettingsItem(
                    icon = "❓",
                    title = "도움말",
                    subtitle = "사용법 및 면책 사항",
                    onClick = { /* TODO */ }
                )
            }
        }
    }
}

@Composable
private fun SettingsItem(
    icon: String,
    title: String,
    subtitle: String? = null,
    value: String? = null,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = icon,
            fontSize = 20.sp,
            modifier = Modifier.size(24.dp)
        )
        
        Spacer(modifier = Modifier.width(12.dp))
        
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium,
                color = Color.Black
            )
            
            if (subtitle != null) {
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )
            }
        }
        
        if (value != null) {
            Surface(
                color = Color(0xFFE0E0E0),
                shape = RoundedCornerShape(20.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = value,
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.Black
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "▼",
                        fontSize = 10.sp,
                        color = Color.Gray
                    )
                }
            }
        } else {
            Text(
                text = "▶",
                fontSize = 16.sp,
                color = Color.Gray
            )
        }
    }
}
