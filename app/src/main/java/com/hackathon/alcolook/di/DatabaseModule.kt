package com.hackathon.alcolook.di

import android.content.Context
import androidx.room.Room
import com.hackathon.alcolook.data.dao.DrinkRecordDao
import com.hackathon.alcolook.data.database.AlcoLookDatabase
import com.hackathon.alcolook.data.repository.UserProfileRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    
    @Provides
    @Singleton
    fun provideAlcoLookDatabase(
        @ApplicationContext context: Context
    ): AlcoLookDatabase {
        return Room.databaseBuilder(
            context.applicationContext,
            AlcoLookDatabase::class.java,
            "alcolook_database"
        ).build()
    }
    
    @Provides
    fun provideDrinkRecordDao(database: AlcoLookDatabase): DrinkRecordDao {
        return database.drinkRecordDao()
    }
    
    @Provides
    @Singleton
    fun provideUserProfileRepository(
        @ApplicationContext context: Context
    ): UserProfileRepository {
        return UserProfileRepository(context)
    }
}