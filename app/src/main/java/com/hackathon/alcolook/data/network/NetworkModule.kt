package com.hackathon.alcolook.data.network

import com.hackathon.alcolook.data.remote.DrinkRecordApiService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {
    
    private const val BASE_URL = "https://iql82o9kv2.execute-api.us-east-1.amazonaws.com/prod/"
    
    @Provides
    @Singleton
    fun provideRetrofit(): Retrofit {
        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }
    
    @Provides
    @Singleton
    fun provideAuthApiService(retrofit: Retrofit): AuthApiService {
        return retrofit.create(AuthApiService::class.java)
    }
    
    @Provides
    @Singleton
    fun provideUserProfileApiService(retrofit: Retrofit): UserProfileApiService {
        return retrofit.create(UserProfileApiService::class.java)
    }
    
    @Provides
    @Singleton
    fun provideDrinkRecordApiService(retrofit: Retrofit): DrinkRecordApiService {
        return retrofit.create(DrinkRecordApiService::class.java)
    }
}
