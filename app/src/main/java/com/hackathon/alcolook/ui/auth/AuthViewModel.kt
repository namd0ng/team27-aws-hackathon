package com.hackathon.alcolook.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hackathon.alcolook.data.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {
    
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading
    
    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage
    
    fun login(email: String, password: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            
            authRepository.login(email, password)
                .onSuccess { response ->
                    // 로그인 성공
                    onSuccess()
                }
                .onFailure { error ->
                    _errorMessage.value = error.message ?: "로그인 실패"
                }
            
            _isLoading.value = false
        }
    }
    
    fun signUp(email: String, password: String, name: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            
            authRepository.signUp(email, password, name)
                .onSuccess { response ->
                    // 회원가입 성공
                    onSuccess()
                }
                .onFailure { error ->
                    _errorMessage.value = error.message ?: "회원가입 실패"
                }
            
            _isLoading.value = false
        }
    }
    
    fun forgotPassword(email: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            
            authRepository.forgotPassword(email)
                .onSuccess { response ->
                    onSuccess()
                }
                .onFailure { error ->
                    _errorMessage.value = error.message ?: "비밀번호 재설정 실패"
                }
            
            _isLoading.value = false
        }
    }
    
    fun clearError() {
        _errorMessage.value = null
    }
}
