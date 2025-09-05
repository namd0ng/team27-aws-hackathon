package com.hackathon.alcolook

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

@Composable
fun getAlcoholLevelColor(level: AlcoholLevel): Color {
    return when (level) {
        AlcoholLevel.NORMAL -> Color.Green
        AlcoholLevel.MILD -> Color(0xFFFF9800)
        AlcoholLevel.MODERATE -> Color(0xFFFF5722)
        AlcoholLevel.HIGH -> Color.Red
        AlcoholLevel.UNKNOWN -> Color.Gray
    }
}

fun getAlcoholLevelText(level: AlcoholLevel): String {
    return when (level) {
        AlcoholLevel.NORMAL -> "정상"
        AlcoholLevel.MILD -> "경미"
        AlcoholLevel.MODERATE -> "보통"
        AlcoholLevel.HIGH -> "높음"
        AlcoholLevel.UNKNOWN -> "알 수 없음"
    }
}
