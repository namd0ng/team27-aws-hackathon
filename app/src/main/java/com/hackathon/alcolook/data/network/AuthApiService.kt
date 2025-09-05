package com.hackathon.alcolook.data.network

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface AuthApiService {
    
    @POST("auth/login")
    suspend fun login(@Body request: LoginRequest): Response<LoginResponse>
    
    @POST("auth/register")
    suspend fun signUp(@Body request: SignUpRequest): Response<SignUpResponse>
    
    @POST("auth/forgotpassword")
    suspend fun forgotPassword(@Body request: ForgotPasswordRequest): Response<ForgotPasswordResponse>
}
