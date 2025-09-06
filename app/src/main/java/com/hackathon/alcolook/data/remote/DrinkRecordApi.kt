package com.hackathon.alcolook.data.remote

import com.hackathon.alcolook.data.model.DrinkRecord
import retrofit2.http.*

interface DrinkRecordApi {
    
    @POST("drink-records")
    suspend fun createRecord(@Body record: DrinkRecord): DrinkRecord
    
    @GET("drink-records")
    suspend fun getRecords(@Query("userId") userId: String): List<DrinkRecord>
    
    @PUT("drink-records")
    suspend fun updateRecord(@Body record: DrinkRecord): DrinkRecord
    
    @DELETE("drink-records")
    suspend fun deleteRecord(@Query("recordId") recordId: String)
}
