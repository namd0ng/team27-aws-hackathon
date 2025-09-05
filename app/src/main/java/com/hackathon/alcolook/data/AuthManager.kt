package com.hackathon.alcolook.data

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class AuthManager private constructor(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("auth_prefs", Context.MODE_PRIVATE)
    
    private val _isLoggedIn = MutableStateFlow(false)
    val isLoggedIn: StateFlow<Boolean> = _isLoggedIn
    
    private val _userName = MutableStateFlow<String?>(null)
    val userName: StateFlow<String?> = _userName
    
    init {
        // 초기 상태 로드
        val savedLoginState = prefs.getBoolean("is_logged_in", false)
        val savedUserName = prefs.getString("user_name", null)
        android.util.Log.d("AuthManager", "Init - savedLoginState: $savedLoginState, savedUserName: $savedUserName")
        
        _isLoggedIn.value = savedLoginState
        _userName.value = savedUserName
    }
    
    fun login(userId: String, email: String, name: String, token: String? = null) {
        android.util.Log.d("AuthManager", "Login called: $userId, $email, $name")
        prefs.edit()
            .putBoolean("is_logged_in", true)
            .putString("user_id", userId)
            .putString("user_email", email)
            .putString("user_name", name)
            .putString("jwt_token", token)
            .apply()
        
        _isLoggedIn.value = true
        _userName.value = name
        android.util.Log.d("AuthManager", "Login state updated: ${_isLoggedIn.value}")
    }
    
    fun getToken(): String? {
        return prefs.getString("jwt_token", null)
    }
    
    fun getUserId(): String? {
        return prefs.getString("user_id", null)
    }
    
    fun logout() {
        prefs.edit()
            .putBoolean("is_logged_in", false)
            .remove("user_id")
            .remove("user_email")
            .remove("user_name")
            .remove("jwt_token")
            .apply()
        
        _isLoggedIn.value = false
        _userName.value = null
    }
    
    companion object {
        @Volatile
        private var INSTANCE: AuthManager? = null
        
        fun getInstance(context: Context): AuthManager {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: AuthManager(context.applicationContext).also { INSTANCE = it }
            }
        }
    }
}
