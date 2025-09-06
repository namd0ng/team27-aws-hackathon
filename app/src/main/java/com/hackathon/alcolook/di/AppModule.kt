package com.hackathon.alcolook.di

import android.content.Context
import com.hackathon.alcolook.data.AuthManager
import com.hackathon.alcolook.data.remote.DynamoDBService
import com.hackathon.alcolook.data.remote.DrinkRecordApiService
import com.hackathon.alcolook.data.repository.DrinkRecordRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {
    
    @Provides
    @Singleton
    fun provideAuthManager(@ApplicationContext context: Context): AuthManager {
        return AuthManager.getInstance(context)
    }
    
    @Provides
    @Singleton
    fun provideRetrofit(): Retrofit {
        return Retrofit.Builder()
            .baseUrl("https://iql82o9kv2.execute-api.us-east-1.amazonaws.com/prod/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }
    
    @Provides
    @Singleton
    fun provideDrinkRecordApiService(retrofit: Retrofit): DrinkRecordApiService {
        return retrofit.create(DrinkRecordApiService::class.java)
    }
    
    @Provides
    @Singleton
    fun provideDynamoDBService(apiService: DrinkRecordApiService): DynamoDBService {
        return DynamoDBService(apiService)
    }
    
    @Provides
    @Singleton
    fun provideDrinkRecordRepository(
        authManager: AuthManager,
        dynamoDBService: DynamoDBService
    ): DrinkRecordRepository {
        return DrinkRecordRepository(authManager, dynamoDBService)
    }
}
