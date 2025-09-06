package com.hackathon.alcolook.data.network

data class LoginRequest(
    val email: String,
    val password: String
)

data class SignUpRequest(
    val email: String,
    val password: String,
    val name: String
)

data class ForgotPasswordRequest(
    val email: String
)

data class LoginResponse(
    val message: String,
    val user_id: String,
    val email: String,
    val name: String,
    val token: String? = null
)

data class SignUpResponse(
    val message: String,
    val user_id: String,
    val email: String,
    val name: String
)

data class ForgotPasswordResponse(
    val message: String
)

data class User(
    val user_id: String,
    val email: String,
    val name: String
)

data class ErrorResponse(
    val error: String
)
