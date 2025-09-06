package com.hackathon.alcolook

import com.hackathon.alcolook.data.model.*
import org.junit.Test
import org.junit.Assert.*
import java.time.LocalDate

class DrinkCalculationTest {

    @Test
    fun `소주 1잔 표준잔수 계산 테스트`() {
        // Given: 소주 1잔 (50ml, 16.9%)
        val record = DrinkRecord(
            id = 1,
            date = LocalDate.now(),
            type = DrinkType.SOJU,
            unit = DrinkUnit.GLASS,
            quantity = 1,
            totalVolumeMl = 50,
            abv = null, // 기본값 사용
            note = null
        )
        
        // When
        val pureAlcohol = record.getPureAlcoholGrams()
        val standardDrinks = record.getStandardDrinks()
        
        // Then
        // 50ml × 16.9% × 0.789 = 6.67g
        assertEquals(6.67f, pureAlcohol, 0.1f)
        // 6.67g ÷ 8g = 0.83표준잔
        assertEquals(0.83f, standardDrinks, 0.1f)
    }

    @Test
    fun `맥주 1캔 표준잔수 계산 테스트`() {
        // Given: 맥주 1캔 (355ml, 4.5%)
        val record = DrinkRecord(
            id = 2,
            date = LocalDate.now(),
            type = DrinkType.BEER,
            unit = DrinkUnit.CAN,
            quantity = 1,
            totalVolumeMl = 355,
            abv = null,
            note = null
        )
        
        // When
        val pureAlcohol = record.getPureAlcoholGrams()
        val standardDrinks = record.getStandardDrinks()
        
        // Then
        // 355ml × 4.5% × 0.789 = 12.6g
        assertEquals(12.6f, pureAlcohol, 0.1f)
        // 12.6g ÷ 8g = 1.58표준잔
        assertEquals(1.58f, standardDrinks, 0.1f)
    }

    @Test
    fun `사용자 정의 도수 계산 테스트`() {
        // Given: 소주 1잔, 사용자가 20% 입력
        val record = DrinkRecord(
            id = 3,
            date = LocalDate.now(),
            type = DrinkType.SOJU,
            unit = DrinkUnit.GLASS,
            quantity = 1,
            totalVolumeMl = 50,
            abv = 20f, // 사용자 입력
            note = null
        )
        
        // When
        val actualAbv = record.getActualAbv()
        val pureAlcohol = record.getPureAlcoholGrams()
        
        // Then
        assertEquals(20f, actualAbv, 0.1f)
        // 50ml × 20% × 0.789 = 7.89g
        assertEquals(7.89f, pureAlcohol, 0.1f)
    }

    @Test
    fun `DrinkUnit 기본 용량 매핑 테스트`() {
        // 소주 잔
        assertEquals(50, DrinkUnit.GLASS.getDefaultVolume(DrinkType.SOJU))
        // 맥주 병
        assertEquals(500, DrinkUnit.BOTTLE.getDefaultVolume(DrinkType.BEER))
        // 맥주 캔
        assertEquals(355, DrinkUnit.CAN.getDefaultVolume(DrinkType.BEER))
        // 위스키 샷
        assertEquals(30, DrinkUnit.SHOT.getDefaultVolume(DrinkType.WHISKY))
    }

    @Test
    fun `UserProfile 권장량 계산 테스트`() {
        // 남성
        val maleProfile = UserProfile(sex = Gender.MALE, isSenior65 = false)
        assertEquals(2f, maleProfile.getDailyRecommendedStdDrinks(), 0.1f)
        assertEquals(14f, maleProfile.getWeeklyRecommendedStdDrinks(), 0.1f)
        assertEquals(5f, maleProfile.getBingeThreshold(), 0.1f)
        
        // 여성
        val femaleProfile = UserProfile(sex = Gender.FEMALE, isSenior65 = false)
        assertEquals(1f, femaleProfile.getDailyRecommendedStdDrinks(), 0.1f)
        assertEquals(7f, femaleProfile.getWeeklyRecommendedStdDrinks(), 0.1f)
        assertEquals(4f, femaleProfile.getBingeThreshold(), 0.1f)
        
        // 65세 이상
        val seniorProfile = UserProfile(sex = Gender.MALE, isSenior65 = true)
        assertEquals(1f, seniorProfile.getDailyRecommendedStdDrinks(), 0.1f)
        assertEquals(7f, seniorProfile.getWeeklyRecommendedStdDrinks(), 0.1f)
    }

    @Test
    fun `복합 음주 시나리오 테스트`() {
        // Given: 소주 2잔 + 맥주 1캔
        val sojuRecord = DrinkRecord(
            id = 1, date = LocalDate.now(), type = DrinkType.SOJU,
            unit = DrinkUnit.GLASS, quantity = 2, totalVolumeMl = 100,
            abv = null, note = null
        )
        
        val beerRecord = DrinkRecord(
            id = 2, date = LocalDate.now(), type = DrinkType.BEER,
            unit = DrinkUnit.CAN, quantity = 1, totalVolumeMl = 355,
            abv = null, note = null
        )
        
        // When
        val totalStandardDrinks = sojuRecord.getStandardDrinks() + beerRecord.getStandardDrinks()
        
        // Then: 약 2.4표준잔 (0.83 × 2 + 1.58)
        assertEquals(2.4f, totalStandardDrinks, 0.2f)
        
        // 남성 기준 일일 권장량(2표준잔) 초과
        val maleProfile = UserProfile(sex = Gender.MALE)
        assertTrue(totalStandardDrinks > maleProfile.getDailyRecommendedStdDrinks())
    }
}