package com.hackathon.alcolook.data.dao

import androidx.room.*
import com.hackathon.alcolook.data.model.DrinkRecordEntity
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

@Dao
interface DrinkRecordDao {
    
    @Query("SELECT * FROM drink_records ORDER BY date DESC, id DESC")
    fun getAllRecords(): Flow<List<DrinkRecordEntity>>
    
    @Query("SELECT * FROM drink_records WHERE date = :date ORDER BY id DESC")
    fun getRecordsByDate(date: LocalDate): Flow<List<DrinkRecordEntity>>
    
    @Query("SELECT * FROM drink_records WHERE date BETWEEN :startDate AND :endDate ORDER BY date DESC, id DESC")
    fun getRecordsBetweenDates(startDate: LocalDate, endDate: LocalDate): Flow<List<DrinkRecordEntity>>
    
    @Query("SELECT * FROM drink_records WHERE id = :id")
    suspend fun getRecordById(id: Long): DrinkRecordEntity?
    
    @Insert
    suspend fun insertRecord(record: DrinkRecordEntity): Long
    
    @Update
    suspend fun updateRecord(record: DrinkRecordEntity)
    
    @Delete
    suspend fun deleteRecord(record: DrinkRecordEntity)
    
    @Query("DELETE FROM drink_records WHERE id = :id")
    suspend fun deleteRecordById(id: Long)
    
    @Query("DELETE FROM drink_records")
    suspend fun deleteAllRecords()
}