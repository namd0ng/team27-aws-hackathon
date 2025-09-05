package com.hackathon.alcolook.domain.usecase

import com.hackathon.alcolook.data.model.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DrinkCalculationUseCase @Inject constructor() {
    
    fun calculateTotalVolume(
        drinkType: DrinkType,
        unit: DrinkUnit,
        quantity: Int,
        customVolumeMl: Int? = null
    ): Int {
        val singleVolume = customVolumeMl ?: unit.getVolumeMl(drinkType)
        return singleVolume * quantity
    }
    
    fun createDrinkRecord(
        date: java.time.LocalDate,
        type: DrinkType,
        unit: DrinkUnit,
        quantity: Int,
        customVolumeMl: Int? = null,
        customAbv: Float? = null,
        note: String? = null
    ): DrinkRecord {
        val totalVolume = calculateTotalVolume(type, unit, quantity, customVolumeMl)
        
        return DrinkRecord(
            date = date,
            type = type,
            unit = unit,
            quantity = quantity,
            totalVolumeMl = totalVolume,
            abv = customAbv,
            note = note
        )
    }
    
    fun getDailyStatus(
        records: List<DrinkRecord>,
        userProfile: UserProfile
    ): DrinkingStatus {
        val totalStandardDrinks = records.sumOf { it.getStandardDrinks().toDouble() }.toFloat()
        val dailyRecommended = userProfile.getDailyRecommendedStdDrinks()
        val bingeThreshold = userProfile.getBingeThreshold()
        
        return when {
            totalStandardDrinks >= bingeThreshold -> DrinkingStatus.DANGER
            totalStandardDrinks > dailyRecommended -> DrinkingStatus.WARNING
            else -> DrinkingStatus.NORMAL
        }
    }
}

enum class DrinkingStatus {
    NORMAL, WARNING, DANGER
}