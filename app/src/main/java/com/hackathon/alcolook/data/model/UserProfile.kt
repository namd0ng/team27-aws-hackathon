package com.hackathon.alcolook.data.model

data class UserProfile(
    val sex: Gender = Gender.UNSET,
    val isSenior65: Boolean = false,
    val weeklyGoalStdDrinks: Int? = null
) {
    // 보건복지부 기준 일일 권장 표준잔수
    fun getDailyRecommendedStdDrinks(): Float = when {
        isSenior65 -> 1f
        sex == Gender.MALE -> 2f
        sex == Gender.FEMALE -> 1f
        else -> 1f // 기본값
    }
    
    // 주간 권장 표준잔수
    fun getWeeklyRecommendedStdDrinks(): Float = when {
        isSenior65 -> 7f
        sex == Gender.MALE -> 14f
        sex == Gender.FEMALE -> 7f
        else -> 7f // 기본값
    }
    
    // 폭음 기준 (1회)
    fun getBingeThreshold(): Float = when {
        sex == Gender.MALE -> 5f
        sex == Gender.FEMALE -> 4f
        else -> 4f // 기본값 (보수적)
    }
}