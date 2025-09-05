package com.hackathon.alcolook

import androidx.compose.ui.graphics.Color

fun getAlcoholLevelColor(level: AlcoholLevel): Color {
    return when (level) {
        AlcoholLevel.NONE -> Color(0xFF4CAF50)
        AlcoholLevel.MINIMAL -> Color(0xFF8BC34A)
        AlcoholLevel.LOW -> Color(0xFFFF9800)
        AlcoholLevel.MODERATE -> Color(0xFFF44336)
        AlcoholLevel.HIGH -> Color(0xFFD32F2F)
    }
}

fun getAlcoholLevelText(level: AlcoholLevel): String {
    return when (level) {
        AlcoholLevel.NONE -> "없음"
        AlcoholLevel.MINIMAL -> "최소"
        AlcoholLevel.LOW -> "경미"
        AlcoholLevel.MODERATE -> "중간"
        AlcoholLevel.HIGH -> "과도"
    }
}
