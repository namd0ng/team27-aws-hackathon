package com.hackathon.alcolook.ui.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hackathon.alcolook.data.AuthManager
import com.hackathon.alcolook.data.repository.DynamoDBProfileRepository
import com.hackathon.alcolook.ui.theme.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onLoginClick: () -> Unit = {}
) {
    val context = LocalContext.current
    val authManager = remember { AuthManager.getInstance(context) }
    val profileRepository = remember { DynamoDBProfileRepository(authManager) }
    val isLoggedIn by authManager.isLoggedIn.collectAsState()
    val userName by authManager.userName.collectAsState()
    
    // 프로필 상태
    var selectedGender by remember { mutableStateOf("설정되지 않음") }
    var ageInput by remember { mutableStateOf("") }
    var weeklyGoalInput by remember { mutableStateOf("") }
    var showSaveDialog by remember { mutableStateOf(false) }
    var saveMessage by remember { mutableStateOf("") }
    
    val scope = rememberCoroutineScope()
    
    // DynamoDB에서 프로필 로드
    LaunchedEffect(isLoggedIn) {
        if (isLoggedIn) {
            profileRepository.loadProfile { success, profile ->
                if (success && profile != null) {
                    selectedGender = when(profile.sex) {
                        "MALE" -> "남성"
                        "FEMALE" -> "여성"
                        else -> "설정되지 않음"
                    }
                    ageInput = profile.age?.toString() ?: ""
                    weeklyGoalInput = profile.weeklyGoalStdDrinks?.toString() ?: ""
                }
            }
        } else {
            // 로그아웃 상태면 초기화
            selectedGender = "설정되지 않음"
            ageInput = ""
            weeklyGoalInput = ""
        }
    }
    
    var isInitialized by remember { mutableStateOf(false) }
    
    // 초기화 완료 후에만 UI 표시
    LaunchedEffect(Unit) {
        kotlinx.coroutines.delay(100) // 짧은 지연으로 상태 로딩 대기
        isInitialized = true
    }
    
    if (!isInitialized) {
        // 로딩 중
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
        return
    }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AppBackground)
    ) {
        // Top Header - 캘린더와 동일한 스타일
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
        
        // 구분선
        HorizontalDivider(
            color = DividerColor,
            thickness = 0.5.dp,
            modifier = Modifier.padding(horizontal = 16.dp)
        )
        
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Account Section
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = CardBackground),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "👤",
                            fontSize = 16.sp
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "계정",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = TextPrimary
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    if (isLoggedIn) {
                        // 로그인된 상태
                        Text(
                            text = "안녕하세요, ${userName ?: "사용자"}님!",
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextPrimary,
                            modifier = Modifier.padding(bottom = 12.dp)
                        )
                        Button(
                            onClick = { authManager.logout() },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.error
                            ),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = "로그아웃",
                                fontWeight = FontWeight.Medium
                            )
                        }
                    } else {
                        // 로그인되지 않은 상태
                        Button(
                            onClick = onLoginClick,
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color.Black
                            ),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = "로그인",
                                color = Color.White,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }

            // Profile Section
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = CardBackground),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "📝",
                            fontSize = 16.sp
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "프로필",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = TextPrimary
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // 성별 선택
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf("남성", "여성", "설정되지 않음").forEach { gender ->
                            FilterChip(
                                onClick = { selectedGender = gender },
                                label = { 
                                    Text(
                                        text = gender,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                },
                                selected = selectedGender == gender,
                                modifier = Modifier.weight(1f),
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = TabSelected.copy(alpha = 0.1f),
                                    selectedLabelColor = TabSelected,
                                    containerColor = Color.Transparent,
                                    labelColor = TabUnselected
                                )
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    OutlinedTextField(
                        value = ageInput,
                        onValueChange = { ageInput = it },
                        label = { Text("연령") },
                        placeholder = { Text("나이를 입력하세요") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = TabSelected,
                            focusedLabelColor = TabSelected
                        ),
                        shape = RoundedCornerShape(8.dp)
                    )
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    OutlinedTextField(
                        value = weeklyGoalInput,
                        onValueChange = { weeklyGoalInput = it },
                        label = { Text("주간 목표 (잔)") },
                        placeholder = { Text("주간 목표 잔수를 입력하세요") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = TabSelected,
                            focusedLabelColor = TabSelected
                        ),
                        shape = RoundedCornerShape(8.dp)
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Button(
                        onClick = { showSaveDialog = true },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.Black
                        ),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = "프로필 저장",
                            color = Color.White,
                            fontWeight = FontWeight.Medium
                        )
                    }
                    
                    // 저장 메시지 표시
                    if (saveMessage.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = if (saveMessage.contains("실패")) 
                                    Color(0xFFFDEBEC) 
                                else 
                                    Color(0xFFE8F5E8)
                            ),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = saveMessage,
                                style = MaterialTheme.typography.bodySmall,
                                color = if (saveMessage.contains("실패")) 
                                    Color(0xFFC62828) 
                                else 
                                    Color(0xFF2E7D32),
                                modifier = Modifier.padding(12.dp)
                            )
                        }
                        
                        // 3초 후 메시지 자동 삭제
                        LaunchedEffect(saveMessage) {
                            if (saveMessage.isNotEmpty()) {
                                kotlinx.coroutines.delay(3000)
                                saveMessage = ""
                            }
                        }
                    }
                }
            }
            
            // Data Management Section
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = CardBackground),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "🗂️",
                            fontSize = 16.sp
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "데이터 관리",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = TextPrimary
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    SettingsItem(
                        title = "데이터 전체 삭제",
                        subtitle = "모든 기록을 삭제합니다",
                        onClick = { /* TODO: Data deletion */ },
                        isDestructive = true
                    )
                }
            }
            
            // Help Section
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = CardBackground),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "❓",
                            fontSize = 16.sp
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "도움말",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = TextPrimary
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    SettingsItem(
                        title = "앱 정보",
                        subtitle = "버전 1.0",
                        onClick = { /* TODO: App info */ }
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    SettingsItem(
                        title = "면책 고지",
                        subtitle = "이용 약관 및 주의사항",
                        onClick = { /* TODO: Disclaimer */ }
                    )
                }
            }
            
            // Disclaimer - 캘린더와 동일한 스타일
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFFFFF4E5)
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "⚠️",
                        fontSize = 24.sp,
                        modifier = Modifier.padding(end = 12.dp)
                    )
                    Text(
                        text = "이 앱은 의료 목적이 아니며, 운전 판단에 사용하지 마세요.\n모든 데이터는 로컬에 저장되며 외부로 전송되지 않습니다.",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextPrimary,
                        lineHeight = 18.sp
                    )
                }
            }
        }
    }
    
    // 프로필 저장 확인 다이얼로그
    if (showSaveDialog) {
        AlertDialog(
            onDismissRequest = { showSaveDialog = false },
            title = { 
                Text(
                    text = "프로필 저장",
                    fontWeight = FontWeight.SemiBold
                ) 
            },
            text = { 
                Text("프로필 정보를 저장하시겠습니까?\n\n성별: $selectedGender\n연령: ${ageInput.ifEmpty { "미입력" }}\n주간 목표: ${weeklyGoalInput.ifEmpty { "미입력" }}잔")
            },
            confirmButton = {
                Button(
                    onClick = {
                        scope.launch {
                            try {
                                // 실시간 로그인 정보 확인
                                val currentLoginState = authManager.isLoggedIn.value
                                val userId = authManager.getUserId()
                                val token = authManager.getToken()
                                val userName = authManager.userName.value
                                
                                android.util.Log.d("SettingsScreen", "=== 로그인 상태 디버그 ===")
                                android.util.Log.d("SettingsScreen", "Compose isLoggedIn: $isLoggedIn")
                                android.util.Log.d("SettingsScreen", "AuthManager isLoggedIn: $currentLoginState")
                                android.util.Log.d("SettingsScreen", "사용자 ID: $userId")
                                android.util.Log.d("SettingsScreen", "사용자 이름: $userName")
                                android.util.Log.d("SettingsScreen", "토큰 존재: ${token != null}")
                                android.util.Log.d("SettingsScreen", "토큰 길이: ${token?.length}")
                                
                                // AuthManager의 실제 상태 사용
                                if (!currentLoginState || userId == null) {
                                    saveMessage = "로그인이 필요합니다 (상태: $currentLoginState, ID: $userId)"
                                    showSaveDialog = false
                                    return@launch
                                }
                                
                                // DynamoDB에 저장
                                val genderValue = when(selectedGender) {
                                    "남성" -> "MALE"
                                    "여성" -> "FEMALE"
                                    else -> "UNSET"
                                }
                                val ageValue = ageInput.toIntOrNull()
                                val goalValue = weeklyGoalInput.toIntOrNull()
                                
                                profileRepository.saveProfile(genderValue, ageValue, goalValue) { success, message ->
                                    android.util.Log.d("SettingsScreen", "DynamoDB 결과: success=$success, message=$message")
                                    if (success) {
                                        saveMessage = "프로필이 저장되었습니다!"
                                        // 저장 성공 시 프로필 다시 로드하여 동기화
                                        profileRepository.loadProfile { loadSuccess, profile ->
                                            if (loadSuccess && profile != null) {
                                                selectedGender = when(profile.sex) {
                                                    "MALE" -> "남성"
                                                    "FEMALE" -> "여성"
                                                    else -> "설정되지 않음"
                                                }
                                                ageInput = profile.age?.toString() ?: ""
                                                weeklyGoalInput = profile.weeklyGoalStdDrinks?.toString() ?: ""
                                            }
                                        }
                                    } else {
                                        saveMessage = "저장 실패: $message"
                                    }
                                }
                                
                                showSaveDialog = false
                            } catch (e: Exception) {
                                android.util.Log.e("SettingsScreen", "저장 오류", e)
                                saveMessage = "저장 실패: ${e.message}"
                                showSaveDialog = false
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.Black
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = "저장",
                        color = Color.White,
                        fontWeight = FontWeight.Medium
                    )
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = { showSaveDialog = false },
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = TextSecondary
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("취소")
                }
            },
            shape = RoundedCornerShape(16.dp)
        )
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
        contentPadding = PaddingValues(vertical = 8.dp, horizontal = 0.dp),
        colors = ButtonDefaults.textButtonColors(
            contentColor = if (isDestructive) MaterialTheme.colorScheme.error else TextPrimary
        ),
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.Start
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium,
                color = if (isDestructive) MaterialTheme.colorScheme.error else TextPrimary
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = if (isDestructive) MaterialTheme.colorScheme.error.copy(alpha = 0.7f) else TextSecondary
            )
        }
    }
}
