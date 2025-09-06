package com.hackathon.alcolook.data.remote

import retrofit2.Response
import retrofit2.http.*

interface DrinkRecordApiService {
    
    @POST("records")
    suspend fun addRecord(@Body request: CreateRecordRequest): Response<ApiResponse<DrinkRecordData>>
    
    @GET("records")
    suspend fun getRecords(@Query("userId") userId: String): Response<ApiResponse<List<DrinkRecordData>>>
    
    @PUT("records")
    suspend fun updateRecord(@Body request: UpdateRecordRequest): Response<ApiResponse<String>>
    
    @DELETE("records")
    suspend fun deleteRecord(
        @Query("userId") userId: String,
        @Query("recordId") recordId: String
    ): Response<ApiResponse<String>>
}

data class CreateRecordRequest(
    val userId: String,
    val date: String,
    val drinkType: String,
    val unit: String? = "CAN",
    val count: Int,
    val volumeMl: Int?,
    val abv: Float?,
    val note: String?
)

data class UpdateRecordRequest(
    val userId: String,
    val recordId: String,
    val date: String,
    val drinkType: String,
    val unit: String? = "CAN",
    val count: Int,
    val volumeMl: Int?,
    val abv: Float?,
    val note: String?
)

data class ApiResponse<T>(
    val success: Boolean,
    val message: String,
    val data: T?
)

data class DrinkRecordData(
    val id: String,
    val userId: String,
    val date: String,
    val type: String,
    val unit: String?,
    val quantity: Int,
    val totalVolumeMl: Int,
    val abv: Float?,
    val note: String?,
    val createdAt: String,
    val updatedAt: String
)
