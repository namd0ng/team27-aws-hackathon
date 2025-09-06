package com.hackathon.alcolook.data.repository

import com.hackathon.alcolook.data.dao.DrinkRecordDao
import com.hackathon.alcolook.data.model.DrinkRecord
import com.hackathon.alcolook.data.model.toEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DrinkRecordRepository @Inject constructor(
    private val drinkRecordDao: DrinkRecordDao
) {
    
    fun getAllRecords(): Flow<List<DrinkRecord>> {
        return drinkRecordDao.getAllRecords().map { entities ->
            entities.map { it.toDrinkRecord() }
        }
    }
    
    fun getRecordsByDate(date: LocalDate): Flow<List<DrinkRecord>> {
        return drinkRecordDao.getRecordsByDate(date).map { entities ->
            entities.map { it.toDrinkRecord() }
        }
    }
    
    fun getRecordsBetweenDates(startDate: LocalDate, endDate: LocalDate): Flow<List<DrinkRecord>> {
        return drinkRecordDao.getRecordsBetweenDates(startDate, endDate).map { entities ->
            entities.map { it.toDrinkRecord() }
        }
    }
    
    suspend fun getRecordById(id: Long): DrinkRecord? {
        return drinkRecordDao.getRecordById(id)?.toDrinkRecord()
    }
    
    suspend fun insertRecord(record: DrinkRecord): Long {
        return drinkRecordDao.insertRecord(record.toEntity())
    }
    
    suspend fun updateRecord(record: DrinkRecord) {
        drinkRecordDao.updateRecord(record.toEntity())
    }
    
    suspend fun deleteRecord(record: DrinkRecord) {
        drinkRecordDao.deleteRecord(record.toEntity())
    }
    
    suspend fun deleteRecordById(id: Long) {
        drinkRecordDao.deleteRecordById(id)
    }
    
    suspend fun deleteAllRecords() {
        drinkRecordDao.deleteAllRecords()
    }
}