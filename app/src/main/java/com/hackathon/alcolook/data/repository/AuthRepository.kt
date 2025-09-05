package com.hackathon.alcolook.data.repository

import com.hackathon.alcolook.data.network.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepository @Inject constructor(
    private val authApiService: AuthApiService
) {
    
    suspend fun login(email: String, password: String): Result<LoginResponse> {
        return try {
            val response = authApiService.login(LoginRequest(email, password))
            if (response.isSuccessful) {
                response.body()?.let { 
                    Result.success(it)
                } ?: Result.failure(Exception("응답 본문이 비어있습니다"))
            } else {
                Result.failure(Exception("로그인 실패: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun signUp(email: String, password: String, name: String): Result<SignUpResponse> {
        return try {
            val response = authApiService.signUp(SignUpRequest(email, password, name))
            if (response.isSuccessful) {
                response.body()?.let { 
                    Result.success(it)
                } ?: Result.failure(Exception("응답 본문이 비어있습니다"))
            } else {
                Result.failure(Exception("회원가입 실패: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun forgotPassword(email: String): Result<ForgotPasswordResponse> {
        return try {
            val response = authApiService.forgotPassword(ForgotPasswordRequest(email))
            if (response.isSuccessful) {
                response.body()?.let { 
                    Result.success(it)
                } ?: Result.failure(Exception("응답 본문이 비어있습니다"))
            } else {
                Result.failure(Exception("비밀번호 재설정 실패: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
