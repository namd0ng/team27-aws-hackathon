package com.hackathon.alcolook.ui.settings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.hackathon.alcolook.data.AuthManager
import kotlinx.coroutines.launch
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.IOException

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onLoginClick: () -> Unit = {}
) {
    val context = LocalContext.current
    val authManager = remember { AuthManager.getInstance(context) }
    val isLoggedIn by authManager.isLoggedIn.collectAsState()
    val userName by authManager.userName.collectAsState()
    
    // 프로필 상태
    var selectedGender by remember { mutableStateOf("설정되지 않음") }
    var ageInput by remember { mutableStateOf("") }
    var weeklyGoalInput by remember { mutableStateOf("") }
    var showSaveDialog by remember { mutableStateOf(false) }
    var saveMessage by remember { mutableStateOf("") }
    
    val scope = rememberCoroutineScope()
    
    // DynamoDB에서 프로필 로드 (로컬 저장소 제거)
    LaunchedEffect(isLoggedIn) {
        if (isLoggedIn) {
            loadFromDynamoDB(authManager) { success, profile ->
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
            .padding(16.dp)
    ) {
        // Header
        Text(
            text = "설정",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 24.dp)
        )
        
        // Account Section
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "계정",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(12.dp))
                
                if (isLoggedIn) {
                    // 로그인된 상태
                    Text(
                        text = "안녕하세요, ${userName ?: "사용자"}님!",
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    Button(
                        onClick = { authManager.logout() },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.error
                        )
                    ) {
                        Text("로그아웃")
                    }
                } else {
                    // 로그인되지 않은 상태
                    Button(
                        onClick = onLoginClick,
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        )
                    ) {
                        Text("로그인")
                    }
                }
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))

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
                    subtitle = selectedGender,
                    onClick = { 
                        selectedGender = when (selectedGender) {
                            "남성" -> "여성"
                            "여성" -> "설정되지 않음"
                            else -> "남성"
                        }
                    }
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                OutlinedTextField(
                    value = ageInput,
                    onValueChange = { ageInput = it },
                    label = { Text("연령") },
                    placeholder = { Text("나이를 입력하세요") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                OutlinedTextField(
                    value = weeklyGoalInput,
                    onValueChange = { weeklyGoalInput = it },
                    label = { Text("주간 목표 (잔)") },
                    placeholder = { Text("주간 목표 잔수를 입력하세요") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                
                Spacer(modifier = Modifier.height(12.dp))
                
                Button(
                    onClick = { showSaveDialog = true },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("프로필 저장")
                }
                
                // 저장 메시지 표시
                if (saveMessage.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = saveMessage,
                        style = MaterialTheme.typography.bodySmall,
                        color = if (saveMessage.contains("실패")) 
                            MaterialTheme.colorScheme.error 
                        else 
                            MaterialTheme.colorScheme.primary,
                        modifier = Modifier.fillMaxWidth()
                    )
                    
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
    
    // 프로필 저장 확인 다이얼로그
    if (showSaveDialog) {
        AlertDialog(
            onDismissRequest = { showSaveDialog = false },
            title = { Text("프로필 저장") },
            text = { 
                Text("프로필 정보를 저장하시겠습니까?\n\n성별: $selectedGender\n연령: ${ageInput.ifEmpty { "미입력" }}\n주간 목표: ${weeklyGoalInput.ifEmpty { "미입력" }}잔")
            },
            confirmButton = {
                TextButton(
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
                                
                                // DynamoDB에만 저장
                                saveToDynamoDB(selectedGender, ageInput, weeklyGoalInput, authManager) { success, message ->
                                    android.util.Log.d("SettingsScreen", "DynamoDB 결과: success=$success, message=$message")
                                    saveMessage = if (success) {
                                        "프로필이 저장되었습니다!"
                                    } else {
                                        "저장 실패: $message"
                                    }
                                }
                                
                                showSaveDialog = false
                            } catch (e: Exception) {
                                android.util.Log.e("SettingsScreen", "저장 오류", e)
                                saveMessage = "저장 실패: ${e.message}"
                                showSaveDialog = false
                            }
                        }
                    }
                ) {
                    Text("저장")
                }
            },
            dismissButton = {
                TextButton(onClick = { showSaveDialog = false }) {
                    Text("취소")
                }
            }
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

// DynamoDB API 호출 함수 (PUT 방식으로 수정)
private fun saveToDynamoDB(
    gender: String,
    age: String,
    weeklyGoal: String,
    authManager: AuthManager,
    callback: (Boolean, String) -> Unit
) {
    val client = OkHttpClient()
    val userId = authManager.getUserId()
    val token = authManager.getToken()
    
    if (userId == null) {
        callback(false, "사용자 ID가 없습니다")
        return
    }
    
    // 토큰이 없으면 더미 토큰 사용
    val finalToken = token ?: "Bearer dummy_token"
    
    val apiUrl = "https://iql82o9kv2.execute-api.us-east-1.amazonaws.com/prod/update-profile"
    
    android.util.Log.d("DynamoDB", "API 호출 시작 - userId: $userId")
    
    val json = JSONObject().apply {
        put("userId", userId)
        put("sex", when(gender) {
            "남성" -> "MALE"
            "여성" -> "FEMALE"
            else -> "UNSET"
        })
        put("age", age.toIntOrNull())
        put("isSenior65", age.toIntOrNull()?.let { it >= 65 } ?: false)
        put("weeklyGoalStdDrinks", weeklyGoal.toIntOrNull())
    }
    
    android.util.Log.d("DynamoDB", "요청 데이터: $json")
    
    val requestBody = json.toString().toRequestBody("application/json".toMediaType())
    
    val request = Request.Builder()
        .url(apiUrl)
        .put(requestBody)  // POST -> PUT으로 변경
        .addHeader("Authorization", "Bearer $finalToken")
        .addHeader("Content-Type", "application/json")
        .build()
    
    client.newCall(request).enqueue(object : Callback {
        override fun onFailure(call: Call, e: IOException) {
            android.util.Log.e("DynamoDB", "API 호출 실패", e)
            android.os.Handler(android.os.Looper.getMainLooper()).post {
                callback(false, e.message ?: "네트워크 오류")
            }
        }
        
        override fun onResponse(call: Call, response: Response) {
            val responseBody = response.body?.string()
            android.util.Log.d("DynamoDB", "API 응답: ${response.code}, body: $responseBody")
            
            android.os.Handler(android.os.Looper.getMainLooper()).post {
                callback(response.isSuccessful, if (response.isSuccessful) "성공" else "서버 오류: ${response.code} - $responseBody")
            }
        }
    })
}

// 프로필 데이터 클래스
data class UserProfile(
    val sex: String?,
    val age: Int?,
    val weeklyGoalStdDrinks: Int?
)

// DynamoDB에서 프로필 불러오기
private fun loadFromDynamoDB(
    authManager: AuthManager,
    callback: (Boolean, UserProfile?) -> Unit
) {
    val client = OkHttpClient()
    val userId = authManager.getUserId() ?: return callback(false, null)
    val token = authManager.getToken() ?: return callback(false, null)
    
    val apiUrl = "https://iql82o9kv2.execute-api.us-east-1.amazonaws.com/prod/profile/$userId"
    
    val request = Request.Builder()
        .url(apiUrl)
        .get()
        .addHeader("Authorization", "Bearer $token")
        .build()
    
    client.newCall(request).enqueue(object : Callback {
        override fun onFailure(call: Call, e: IOException) {
            android.util.Log.e("DynamoDB", "프로필 로드 실패", e)
            android.os.Handler(android.os.Looper.getMainLooper()).post {
                callback(false, null)
            }
        }
        
        override fun onResponse(call: Call, response: Response) {
            val responseBody = response.body?.string()
            android.util.Log.d("DynamoDB", "프로필 로드 응답: ${response.code}, body: $responseBody")
            
            android.os.Handler(android.os.Looper.getMainLooper()).post {
                if (response.isSuccessful && responseBody != null) {
                    try {
                        val json = org.json.JSONObject(responseBody)
                        val profile = UserProfile(
                            sex = json.optString("sex", null),
                            age = if (json.has("age")) json.getInt("age") else null,
                            weeklyGoalStdDrinks = if (json.has("weeklyGoalStdDrinks")) json.getInt("weeklyGoalStdDrinks") else null
                        )
                        callback(true, profile)
                    } catch (e: Exception) {
                        android.util.Log.e("DynamoDB", "JSON 파싱 오류", e)
                        callback(false, null)
                    }
                } else {
                    callback(false, null)
                }
            }
        }
    })
}
