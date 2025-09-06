package com.hackathon.alcolook.data.network

import retrofit2.Response
import retrofit2.http.*

interface UserProfileApiService {
    
    @GET("profile/{userId}")
    suspend fun getUserProfile(@Path("userId") userId: String): Response<UserProfileResponse>
    
    @PUT("profile/{userId}")
    suspend fun updateUserProfile(
        @Path("userId") userId: String,
        @Body request: UserProfileRequest
    ): Response<UserProfileResponse>
}

data class UserProfileRequest(
    val sex: String,
    val isSenior65: Boolean,
    val weeklyGoalStdDrinks: Int?
)

data class UserProfileResponse(
    val user_id: String,
    val sex: String,
    val isSenior65: Boolean,
    val weeklyGoalStdDrinks: Int?,
    val updated_at: String
)
