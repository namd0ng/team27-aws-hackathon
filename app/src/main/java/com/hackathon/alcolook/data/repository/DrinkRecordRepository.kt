package com.hackathon.alcolook.data.repository

import android.util.Log
import com.hackathon.alcolook.data.AuthManager
import com.hackathon.alcolook.data.model.DrinkRecord
import com.hackathon.alcolook.data.remote.DynamoDBService
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DrinkRecordRepository @Inject constructor(
    private val authManager: AuthManager,
    private val dynamoDBService: DynamoDBService
) {
    
    private val _records = MutableStateFlow<List<DrinkRecord>>(emptyList())
    val records: Flow<List<DrinkRecord>> = _records.asStateFlow()
    
    init {
        // 앱 시작시 DynamoDB에서 데이터 로드
        loadRecordsFromDynamoDB()
    }
    
    suspend fun addRecord(record: DrinkRecord) {
        Log.d("DrinkRecordRepository", "🔥 Starting addRecord...")
        
        withContext(Dispatchers.IO) {
            try {
                Log.d("DrinkRecordRepository", "📱 Creating HTTP client...")
                
                val client = okhttp3.OkHttpClient.Builder()
                    .connectTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
                    .readTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
                    .build()
                
                val json = """{"userId":"app-user","date":"2024-01-20","drinkType":"BEER","count":1,"volumeMl":355,"abv":4.5,"note":"app test"}"""
                
                Log.d("DrinkRecordRepository", "📄 JSON: $json")
                
                val mediaType = "application/json; charset=utf-8".toMediaTypeOrNull()
                val body = okhttp3.RequestBody.create(mediaType, json)
                
                val request = okhttp3.Request.Builder()
                    .url("https://iql82o9kv2.execute-api.us-east-1.amazonaws.com/prod/records")
                    .post(body)
                    .build()
                
                Log.d("DrinkRecordRepository", "🌐 Making network call...")
                
                val response = client.newCall(request).execute()
                val responseBody = response.body?.string()
                
                Log.d("DrinkRecordRepository", "📥 Response code: ${response.code}")
                Log.d("DrinkRecordRepository", "📥 Response body: $responseBody")
                
                if (response.isSuccessful) {
                    Log.d("DrinkRecordRepository", "✅ SUCCESS!")
                } else {
                    Log.e("DrinkRecordRepository", "❌ HTTP ERROR: ${response.code}")
                }
                
            } catch (e: Exception) {
                Log.e("DrinkRecordRepository", "💥 EXCEPTION: ${e.message}", e)
            }
        }
        
        // 항상 로컬에 저장
        addRecordLocally(record)
    }
    
    suspend fun updateRecord(record: DrinkRecord) {
        val userId = getCurrentUserId()
        
        if (userId != null) {
            val result = dynamoDBService.updateRecord(record, userId)
            if (result.isSuccess) {
                updateRecordLocally(record)
            } else {
                Log.e("DrinkRecordRepository", "Failed to update in DynamoDB")
            }
        } else {
            updateRecordLocally(record)
        }
    }
    
    suspend fun deleteRecord(recordId: Long) {
        val userId = getCurrentUserId()
        
        if (userId != null) {
            val result = dynamoDBService.deleteRecord(recordId, userId)
            if (result.isSuccess) {
                deleteRecordLocally(recordId)
            } else {
                Log.e("DrinkRecordRepository", "Failed to delete from DynamoDB")
            }
        } else {
            deleteRecordLocally(recordId)
        }
    }
    
    suspend fun loadRecords() {
        loadRecordsFromDynamoDB()
    }
    
    suspend fun getRecordsByDate(date: LocalDate): List<DrinkRecord> {
        return _records.value.filter { it.date == date }
    }
    
    private fun loadRecordsFromDynamoDB() {
        // 코루틴에서 실행하도록 수정 필요
        // 현재는 초기화시에만 호출되므로 빈 구현
        Log.d("DrinkRecordRepository", "Loading records from DynamoDB...")
    }
    
    private fun addRecordLocally(record: DrinkRecord) {
        val currentRecords = _records.value.toMutableList()
        currentRecords.add(record.copy(id = generateId()))
        _records.value = currentRecords
        Log.d("DrinkRecordRepository", "Added record locally: ${record.type}")
    }
    
    private fun updateRecordLocally(record: DrinkRecord) {
        val currentRecords = _records.value.toMutableList()
        val index = currentRecords.indexOfFirst { it.id == record.id }
        if (index != -1) {
            currentRecords[index] = record
            _records.value = currentRecords
        }
    }
    
    private fun deleteRecordLocally(recordId: Long) {
        val currentRecords = _records.value.toMutableList()
        currentRecords.removeAll { it.id == recordId }
        _records.value = currentRecords
    }
    
    private suspend fun getCurrentUserId(): String? {
        return try {
            val isLoggedIn = authManager.isLoggedIn.first()
            Log.d("DrinkRecordRepository", "isLoggedIn: $isLoggedIn")
            
            if (isLoggedIn) {
                val userName = authManager.userName.first()
                Log.d("DrinkRecordRepository", "userName from AuthManager: $userName")
                
                // 로그인 상태라면 항상 사용자 ID 반환 (userName이 null이어도 기본값 사용)
                userName ?: "logged-in-user"
            } else {
                Log.d("DrinkRecordRepository", "User not logged in - using local storage")
                null
            }
        } catch (e: Exception) {
            Log.e("DrinkRecordRepository", "Error getting user ID", e)
            // 에러 발생시에도 로그인 상태 확인해서 기본값 사용
            "error-fallback-user"
        }
    }
    
    private fun generateId(): Long = System.currentTimeMillis()
}
