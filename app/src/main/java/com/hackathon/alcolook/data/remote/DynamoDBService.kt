package com.hackathon.alcolook.data.remote

import android.util.Log
import com.hackathon.alcolook.data.model.DrinkRecord
import com.hackathon.alcolook.data.model.DrinkType
import com.hackathon.alcolook.data.model.DrinkUnit
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DynamoDBService @Inject constructor(
    private val apiService: DrinkRecordApiService
) {
    
    suspend fun addRecord(record: DrinkRecord, userId: String): Result<DrinkRecord> {
        return withContext(Dispatchers.IO) {
            try {
                Log.d("DynamoDBService", "Adding record to DynamoDB for user: $userId")
                
                val request = CreateRecordRequest(
                    userId = userId,
                    date = record.date.format(DateTimeFormatter.ISO_LOCAL_DATE),
                    drinkType = record.type.name,
                    unit = record.unit.name,
                    count = record.quantity,
                    volumeMl = record.totalVolumeMl,
                    abv = record.abv,
                    note = record.note
                )
                
                Log.d("DynamoDBService", "API Request: $request")
                
                val response = apiService.addRecord(request)
                
                Log.d("DynamoDBService", "API Response code: ${response.code()}")
                
                if (response.isSuccessful) {
                    val apiResponse = response.body()
                    Log.d("DynamoDBService", "API Response: $apiResponse")
                    
                    if (apiResponse?.success == true && apiResponse.data != null) {
                        val savedRecord = apiResponse.data.toDrinkRecord()
                        Log.d("DynamoDBService", "Successfully saved record: ${savedRecord.id}")
                        Result.success(savedRecord)
                    } else {
                        val errorMsg = apiResponse?.message ?: "Unknown error"
                        Log.e("DynamoDBService", "API Error: $errorMsg")
                        Result.failure(Exception(errorMsg))
                    }
                } else {
                    val errorMsg = "HTTP ${response.code()}: ${response.message()}"
                    val errorBody = response.errorBody()?.string()
                    Log.e("DynamoDBService", "API Error: $errorMsg")
                    Log.e("DynamoDBService", "Error body: $errorBody")
                    Result.failure(Exception(errorMsg))
                }
            } catch (e: Exception) {
                Log.e("DynamoDBService", "Network error while adding record", e)
                Result.failure(e)
            }
        }
    }
    
    suspend fun getRecords(userId: String): Result<List<DrinkRecord>> {
        return withContext(Dispatchers.IO) {
            try {
                Log.d("DynamoDBService", "Getting records from DynamoDB for user: $userId")
                
                val response = apiService.getRecords(userId)
                
                Log.d("DynamoDBService", "Get records response code: ${response.code()}")
                
                if (response.isSuccessful) {
                    val apiResponse = response.body()
                    Log.d("DynamoDBService", "API Response: $apiResponse")
                    
                    if (apiResponse?.success == true && apiResponse.data != null) {
                        val records = apiResponse.data.map { it.toDrinkRecord() }
                        Log.d("DynamoDBService", "Successfully loaded ${records.size} records")
                        Result.success(records)
                    } else {
                        val errorMsg = apiResponse?.message ?: "Unknown error"
                        Log.e("DynamoDBService", "API Error: $errorMsg")
                        Result.failure(Exception(errorMsg))
                    }
                } else {
                    val errorMsg = "HTTP ${response.code()}: ${response.message()}"
                    Log.e("DynamoDBService", "API Error: $errorMsg")
                    Result.failure(Exception(errorMsg))
                }
            } catch (e: Exception) {
                Log.e("DynamoDBService", "Network error while getting records", e)
                Result.failure(e)
            }
        }
    }
    
    suspend fun updateRecord(record: DrinkRecord, userId: String): Result<DrinkRecord> {
        return withContext(Dispatchers.IO) {
            try {
                Log.d("DynamoDBService", "Updating record ${record.id} in DynamoDB")
                
                val request = UpdateRecordRequest(
                    userId = userId,
                    recordId = record.id.toString(),
                    date = record.date.format(DateTimeFormatter.ISO_LOCAL_DATE),
                    drinkType = record.type.name,
                    unit = record.unit.name,
                    count = record.quantity,
                    volumeMl = record.totalVolumeMl,
                    abv = record.abv,
                    note = record.note
                )
                
                val response = apiService.updateRecord(request)
                
                if (response.isSuccessful) {
                    val apiResponse = response.body()
                    if (apiResponse?.success == true) {
                        Log.d("DynamoDBService", "Successfully updated record: ${record.id}")
                        Result.success(record)
                    } else {
                        val errorMsg = apiResponse?.message ?: "Unknown error"
                        Log.e("DynamoDBService", "Update API Error: $errorMsg")
                        Result.failure(Exception(errorMsg))
                    }
                } else {
                    val errorMsg = "HTTP ${response.code()}: ${response.message()}"
                    Log.e("DynamoDBService", "Update API Error: $errorMsg")
                    Result.failure(Exception(errorMsg))
                }
            } catch (e: Exception) {
                Log.e("DynamoDBService", "Network error while updating record", e)
                Result.failure(e)
            }
        }
    }
    
    suspend fun deleteRecord(recordId: Long, userId: String): Result<Unit> {
        return withContext(Dispatchers.IO) {
            try {
                Log.d("DynamoDBService", "Deleting record $recordId from DynamoDB")
                
                val response = apiService.deleteRecord(userId, recordId.toString())
                
                if (response.isSuccessful) {
                    val apiResponse = response.body()
                    if (apiResponse?.success == true) {
                        Log.d("DynamoDBService", "Successfully deleted record: $recordId")
                        Result.success(Unit)
                    } else {
                        val errorMsg = apiResponse?.message ?: "Unknown error"
                        Log.e("DynamoDBService", "Delete API Error: $errorMsg")
                        Result.failure(Exception(errorMsg))
                    }
                } else {
                    val errorMsg = "HTTP ${response.code()}: ${response.message()}"
                    Log.e("DynamoDBService", "Delete API Error: $errorMsg")
                    Result.failure(Exception(errorMsg))
                }
            } catch (e: Exception) {
                Log.e("DynamoDBService", "Network error while deleting record", e)
                Result.failure(e)
            }
        }
    }
}

fun DrinkRecordData.toDrinkRecord(): DrinkRecord {
    return DrinkRecord(
        id = this.id.hashCode().toLong(),
        date = LocalDate.parse(this.date, DateTimeFormatter.ISO_LOCAL_DATE),
        type = DrinkType.valueOf(this.type),
        unit = DrinkUnit.valueOf(this.unit ?: "CAN"),
        quantity = this.quantity,
        totalVolumeMl = this.totalVolumeMl,
        abv = this.abv,
        note = this.note
    )
}
