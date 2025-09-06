package com.hackathon.alcolook.data.remote

import com.hackathon.alcolook.data.model.DrinkRecord
import com.hackathon.alcolook.data.model.DrinkType
import com.hackathon.alcolook.data.model.DrinkUnit
import java.time.LocalDate
import java.time.format.DateTimeFormatter

data class DynamoDBDrinkRecord(
    val id: String,
    val userId: String,
    val date: String, // ISO format: "2024-01-15"
    val type: String,
    val unit: String,
    val quantity: Int,
    val totalVolumeMl: Int,
    val abv: Float?,
    val note: String?,
    val createdAt: String,
    val updatedAt: String
) {
    companion object {
        fun fromDrinkRecord(record: DrinkRecord, userId: String): DynamoDBDrinkRecord {
            val now = System.currentTimeMillis().toString()
            return DynamoDBDrinkRecord(
                id = record.id.toString(),
                userId = userId,
                date = record.date.format(DateTimeFormatter.ISO_LOCAL_DATE),
                type = record.type.name,
                unit = record.unit.name,
                quantity = record.quantity,
                totalVolumeMl = record.totalVolumeMl,
                abv = record.abv,
                note = record.note,
                createdAt = now,
                updatedAt = now
            )
        }
    }
    
    fun toDrinkRecord(): DrinkRecord {
        return DrinkRecord(
            id = id.toLongOrNull() ?: 0L,
            date = LocalDate.parse(date, DateTimeFormatter.ISO_LOCAL_DATE),
            type = DrinkType.valueOf(type),
            unit = DrinkUnit.valueOf(unit),
            quantity = quantity,
            totalVolumeMl = totalVolumeMl,
            abv = abv,
            note = note
        )
    }
}
