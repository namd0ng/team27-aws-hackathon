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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hackathon.alcolook.R
import com.hackathon.alcolook.ui.components.DropdownSettingsItem
import com.hackathon.alcolook.ui.components.rememberSettingsState
import com.hackathon.alcolook.ui.theme.*
import com.hackathon.alcolook.data.AuthManager
import com.hackathon.alcolook.data.repository.DynamoDBProfileRepository
import kotlinx.coroutines.launch

/**
 * 설정 화면 - UI와 기능 통합
 */
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
    
    // UI 상태 관리
    var showHelpDialog by remember { mutableStateOf(false) }
    var showSaveDialog by remember { mutableStateOf(false) }
    var saveMessage by remember { mutableStateOf("") }
    
    val genderState = rememberSettingsState(stringResource(R.string.gender_male))
    val ageInputState = rememberSettingsState("")
    val weeklyGoalInputState = rememberSettingsState("")
    val themeState = rememberSettingsState(stringResource(R.string.theme_system))
    
    val scope = rememberCoroutineScope()
    
    // DynamoDB에서 프로필 로드
    LaunchedEffect(isLoggedIn) {
        if (isLoggedIn) {
            profileRepository.loadProfile { success, profile ->
                if (success && profile != null) {
                    genderState.value = when(profile.sex) {
                        "MALE" -> context.getString(R.string.gender_male)
                        "FEMALE" -> context.getString(R.string.gender_female)
                        else -> "설정되지 않음"
                    }
                    ageInputState.value = profile.age?.toString() ?: ""
                    weeklyGoalInputState.value = profile.weeklyGoalStdDrinks?.toString() ?: ""
                }
            }
        } else {
            // 로그아웃 상태면 초기화
            genderState.value = "설정되지 않음"
            ageInputState.value = ""
            weeklyGoalInputState.value = ""
        }
    }
    
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
        
        // 계정 섹션
        AccountSection(
            isLoggedIn = isLoggedIn,
            userName = userName,
            onLoginClick = onLoginClick,
            onLogoutClick = { authManager.logout() }
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // 개인 정보 섹션 (기존 alldata+calendar)
        PersonalInfoSection(
            genderState = genderState,
            ageInputState = ageInputState,
            weeklyGoalInputState = weeklyGoalInputState,
            themeState = themeState,
            onSaveClick = { showSaveDialog = true }
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // 데이터 관리 섹션
        DataManagementSection()
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // 앱 정보 섹션
        AppInfoSection(
            onHelpClick = { showHelpDialog = true }
        )
        
        Spacer(modifier = Modifier.height(100.dp))
    }
    
    // 프로필 저장 확인 다이얼로그 (develop)
    if (showSaveDialog) {
        SaveProfileDialog(
            gender = genderState.value,
            age = ageInputState.value,
            weeklyGoal = weeklyGoalInputState.value,
            onDismiss = { showSaveDialog = false },
            onConfirm = {
                scope.launch {
                    val genderValue = when(genderState.value) {
                        context.getString(R.string.gender_male) -> "MALE"
                        context.getString(R.string.gender_female) -> "FEMALE"
                        else -> "UNSET"
                    }
                    val ageValue = ageInputState.value.toIntOrNull()
                    val goalValue = weeklyGoalInputState.value.toIntOrNull()
                    
                    if (!isLoggedIn || authManager.getUserId() == null) {
                        saveMessage = "로그인이 필요합니다"
                        showSaveDialog = false
                        return@launch
                    }
                    
                    profileRepository.saveProfile(genderValue, ageValue, goalValue) { success, message ->
                        if (success) {
                            saveMessage = "프로필이 저장되었습니다!"
                        } else {
                            saveMessage = "저장 실패: $message"
                        }
                    }
                    showSaveDialog = false
                }
            }
        )
    }

    // 도움말 다이얼로그 (alldata+calendar)
    if (showHelpDialog) {
        HelpDialog(
            onDismiss = { showHelpDialog = false }
        )
    }
}

@Composable
private fun AccountSection(
    isLoggedIn: Boolean,
    userName: String?,
    onLoginClick: () -> Unit,
    onLogoutClick: () -> Unit
) {
    Column(modifier = Modifier.padding(horizontal = 16.dp)) {
        Text(
            text = "계정",
            modifier = Modifier.padding(vertical = 8.dp),
            style = MaterialTheme.typography.titleMedium,
            color = Color.Gray
        )
        
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                if (isLoggedIn) {
                    Text(
                        text = "안녕하세요, ${userName ?: "사용자"}님!",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium,
                        color = Color.Black,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )
                    Button(
                        onClick = onLogoutClick,
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(text = "로그아웃", fontWeight = FontWeight.Medium, color = Color.White)
                    }
                } else {
                    Button(
                        onClick = onLoginClick,
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Black),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(text = "로그인", color = Color.White, fontWeight = FontWeight.Medium)
                    }
                }
            }
        }
    }
}

@Composable
private fun PersonalInfoSection(
    genderState: MutableState<String>,
    ageInputState: MutableState<String>,
    weeklyGoalInputState: MutableState<String>,
    themeState: MutableState<String>,
    onSaveClick: () -> Unit
) {
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
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                DropdownSettingsItem(
                    emoji = "👤",
                    title = "성별",
                    options = listOf(
                        stringResource(R.string.gender_male),
                        stringResource(R.string.gender_female),
                        "설정되지 않음"
                    ),
                    selectedValue = genderState.value,
                    onValueChange = { genderState.value = it }
                )
                
                OutlinedTextField(
                    value = ageInputState.value,
                    onValueChange = { ageInputState.value = it },
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
                
                OutlinedTextField(
                    value = weeklyGoalInputState.value,
                    onValueChange = { weeklyGoalInputState.value = it },
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

                Button(
                    onClick = onSaveClick,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Black),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(text = "프로필 저장", color = Color.White, fontWeight = FontWeight.Medium)
                }
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
private fun AppInfoSection(
    onHelpClick: () -> Unit
) {
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
                    onClick = onHelpClick
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
        
        Text(
            text = "▶",
            fontSize = 16.sp,
            color = Color.Gray
        )
    }
}

@Composable
private fun SaveProfileDialog(
    gender: String,
    age: String,
    weeklyGoal: String,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "프로필 저장",
                fontWeight = FontWeight.SemiBold
            )
        },
        text = {
            Text("프로필 정보를 저장하시겠습니까?\n\n성별: $gender\n연령: ${age.ifEmpty { "미입력" }}\n주간 목표: ${weeklyGoal.ifEmpty { "미입력" }}잔")
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(containerColor = Color.Black),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(text = "저장", color = Color.White, fontWeight = FontWeight.Medium)
            }
        },
        dismissButton = {
            OutlinedButton(
                onClick = onDismiss,
                colors = ButtonDefaults.outlinedButtonColors(contentColor = TextSecondary),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("취소")
            }
        },
        shape = RoundedCornerShape(16.dp)
    )
}

@Composable
private fun HelpDialog(
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "도움말 및 면책 사항",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )
        },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState())
            ) {
                Text(
                    text = "사용 안내",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF2196F3)
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "본격 결과는 참고 지표입니다.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Black
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "법적 고지 및 면책",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF2196F3)
                )

                Spacer(modifier = Modifier.height(8.dp))

                val disclaimerText = """
                    • 이 앱은 의료기기/진단 도구가 아닙니다. 질병의 진단·치료·예방 목적에 사용할 수 없습니다.
                    • 결과는 혈중알코올농도(BAC) 측정기를 대체하지 않습니다.
                    • 운전 가능 여부 판단에 절대 사용하지 마세요.
                    • 결과는 조명·각도·표정 등 환경에 따라 부정확할 수 있습니다. 오판 책임은 사용자에게 있습니다.
                    • 이 앱은 온디바이스로 동작하며, 기본적으로 서버 전송을 하지 않습니다. 설정에서 데이터 전체 삭제가 가능합니다.
                    • 응급 상황(알코올 중독 의심, 의식 저하 등)에서는 즉시 지역 응급번호로 연락하거나 의료기관을 이용하세요.
                """.trimIndent()

                Text(
                    text = disclaimerText,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Black,
                    lineHeight = 18.sp
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "데이터 관리: 설정 > 데이터 관리",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = onDismiss,
                colors = ButtonDefaults.textButtonColors(
                    contentColor = Color(0xFF2196F3)
                )
            ) {
                Text(
                    text = "확인",
                    fontWeight = FontWeight.Medium
                )
            }
        },
        containerColor = Color.White,
        shape = RoundedCornerShape(16.dp)
    )
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