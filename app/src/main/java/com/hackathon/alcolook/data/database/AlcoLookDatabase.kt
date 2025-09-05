package com.hackathon.alcolook.data.database

import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import android.content.Context
import com.hackathon.alcolook.data.dao.DrinkRecordDao
import com.hackathon.alcolook.data.model.DrinkRecordEntity

@Database(
    entities = [DrinkRecordEntity::class],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AlcoLookDatabase : RoomDatabase() {
    
    abstract fun drinkRecordDao(): DrinkRecordDao
    
    companion object {
        @Volatile
        private var INSTANCE: AlcoLookDatabase? = null
        
        fun getDatabase(context: Context): AlcoLookDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AlcoLookDatabase::class.java,
                    "alcolook_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}