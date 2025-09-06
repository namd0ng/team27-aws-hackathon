package com.hackathon.alcolook.data.model

import java.time.LocalDate

data class DrinkRecord(
    val id: Long = 0,
    val date: LocalDate,
    val type: DrinkType,
    val unit: DrinkUnit,
    val quantity: Int,
    val totalVolumeMl: Int,  // 최종 계산된 총 용량
    val abv: Float?,         // 사용자 입력 도수 (null이면 기본값 사용)
    val note: String?        // 메모
) {
    // 실제 사용할 도수 계산
    fun getActualAbv(): Float = abv ?: type.getDefaultAbv()
    
    // 순수 알코올량 계산 (g)
    fun getPureAlcoholGrams(): Float {
        return totalVolumeMl * getActualAbv() * 0.789f / 100f
    }
    
    // 표준잔수 계산 (순수 알코올 8g = 1표준잔)
    fun getStandardDrinks(): Float {
        return getPureAlcoholGrams() / 8f
    }
    
    // 표준잔수 포맷팅 (1자리)
    fun getFormattedStandardDrinks(): String {
        return String.format("%.1f", getStandardDrinks())
    }
}