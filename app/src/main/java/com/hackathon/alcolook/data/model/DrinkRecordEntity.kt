package com.hackathon.alcolook.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDate

@Entity(tableName = "drink_records")
data class DrinkRecordEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val date: LocalDate,
    val type: DrinkType,
    val unit: DrinkUnit,
    val quantity: Int,
    val totalVolumeMl: Int,
    val abv: Float?,
    val note: String?
) {
    fun toDrinkRecord(): DrinkRecord {
        return DrinkRecord(
            id = id,
            date = date,
            type = type,
            unit = unit,
            quantity = quantity,
            totalVolumeMl = totalVolumeMl,
            abv = abv,
            note = note
        )
    }
}

fun DrinkRecord.toEntity(): DrinkRecordEntity {
    return DrinkRecordEntity(
        id = id,
        date = date,
        type = type,
        unit = unit,
        quantity = quantity,
        totalVolumeMl = totalVolumeMl,
        abv = abv,
        note = note
    )
}