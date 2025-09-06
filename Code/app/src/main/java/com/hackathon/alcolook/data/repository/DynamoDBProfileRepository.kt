package com.hackathon.alcolook.data.repository

import com.hackathon.alcolook.data.AuthManager
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

data class DynamoDBUserProfile(
    val sex: String?,
    val age: Int?,
    val weeklyGoalStdDrinks: Int?
)

@Singleton
class DynamoDBProfileRepository @Inject constructor(
    private val authManager: AuthManager
) {
    private val client = OkHttpClient()
    private val updateApiUrl = "https://iql82o9kv2.execute-api.us-east-1.amazonaws.com/prod/profile"
    private val profileApiUrl = "https://iql82o9kv2.execute-api.us-east-1.amazonaws.com/prod/profile"
    
    fun saveProfile(
        sex: String,
        age: Int?,
        weeklyGoalStdDrinks: Int?,
        callback: (Boolean, String) -> Unit
    ) {
        val userId = authManager.getUserId()
        val token = authManager.getToken()
        
        if (userId == null) {
            callback(false, "사용자 ID가 없습니다")
            return
        }
        
        // 토큰 확인 및 디버그
        if (token == null) {
            android.util.Log.w("DynamoDBProfileRepository", "JWT 토큰이 없습니다")
            callback(false, "JWT 토큰이 없습니다. 다시 로그인해주세요.")
            return
        }
        
        android.util.Log.d("DynamoDBProfileRepository", "토큰 사용: ${token.take(20)}...")
        android.util.Log.d("DynamoDBProfileRepository", "사용자 ID: $userId")
        
        val json = JSONObject().apply {
            put("userId", userId)
            put("sex", sex)
            put("age", age)
            put("isSenior65", age?.let { it >= 65 } ?: false)
            put("weeklyGoalStdDrinks", weeklyGoalStdDrinks)
        }
        
        val requestBody = json.toString().toRequestBody("application/json".toMediaType())
        
        val request = Request.Builder()
            .url(updateApiUrl)
            .put(requestBody)
            .addHeader("Authorization", "Bearer $token")
            .addHeader("Content-Type", "application/json")
            .build()
        
        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                android.os.Handler(android.os.Looper.getMainLooper()).post {
                    callback(false, e.message ?: "네트워크 오류")
                }
            }
            
            override fun onResponse(call: Call, response: Response) {
                android.os.Handler(android.os.Looper.getMainLooper()).post {
                    callback(response.isSuccessful, if (response.isSuccessful) "성공" else "서버 오류: ${response.code}")
                }
            }
        })
    }
    
    fun loadProfile(callback: (Boolean, DynamoDBUserProfile?) -> Unit) {
        val userId = authManager.getUserId()
        val token = authManager.getToken()
        
        if (userId == null || token == null) {
            callback(false, null)
            return
        }
        
        val profileGetUrl = "$profileApiUrl/$userId"
        
        val request = Request.Builder()
            .url(profileGetUrl)
            .get()
            .addHeader("Authorization", "Bearer $token")
            .build()
        
        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                android.os.Handler(android.os.Looper.getMainLooper()).post {
                    callback(false, null)
                }
            }
            
            override fun onResponse(call: Call, response: Response) {
                val responseBody = response.body?.string()
                
                android.os.Handler(android.os.Looper.getMainLooper()).post {
                    if (response.isSuccessful && responseBody != null) {
                        try {
                            val json = JSONObject(responseBody)
                            val profile = DynamoDBUserProfile(
                                sex = json.optString("sex", null),
                                age = if (json.has("age")) json.getInt("age") else null,
                                weeklyGoalStdDrinks = if (json.has("weeklyGoalStdDrinks")) json.getInt("weeklyGoalStdDrinks") else null
                            )
                            callback(true, profile)
                        } catch (e: Exception) {
                            callback(false, null)
                        }
                    } else {
                        callback(false, null)
                    }
                }
            }
        })
    }
}
