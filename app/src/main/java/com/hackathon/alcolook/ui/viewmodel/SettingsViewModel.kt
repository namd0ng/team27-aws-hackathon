package com.hackathon.alcolook.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hackathon.alcolook.data.model.UserProfile
import com.hackathon.alcolook.data.model.Gender
import com.hackathon.alcolook.data.repository.UserProfileRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val userProfileRepository: UserProfileRepository
) : ViewModel() {
    
    private val _userProfile = MutableStateFlow(UserProfile())
    val userProfile: StateFlow<UserProfile> = _userProfile
    
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading
    
    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage
    
    private val _saveSuccess = MutableStateFlow(false)
    val saveSuccess: StateFlow<Boolean> = _saveSuccess
    
    init {
        loadUserProfile()
    }
    
    private fun loadUserProfile() {
        viewModelScope.launch {
            _userProfile.value = userProfileRepository.getUserProfile()
        }
    }
    
    fun updateSex(sex: Gender) {
        _userProfile.value = _userProfile.value.copy(sex = sex)
    }
    
    fun updateIsSenior65(isSenior65: Boolean) {
        _userProfile.value = _userProfile.value.copy(isSenior65 = isSenior65)
    }
    
    fun updateWeeklyGoal(weeklyGoal: Int?) {
        _userProfile.value = _userProfile.value.copy(weeklyGoalStdDrinks = weeklyGoal)
    }
    
    fun saveProfile(userId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            _saveSuccess.value = false
            
            try {
                // 로컬 저장
                userProfileRepository.saveUserProfile(_userProfile.value)
                _saveSuccess.value = true
            } catch (error: Exception) {
                _errorMessage.value = error.message ?: "프로필 저장 실패"
            }
            
            _isLoading.value = false
        }
    }
    
    fun clearMessages() {
        _errorMessage.value = null
        _saveSuccess.value = false
    }
}
