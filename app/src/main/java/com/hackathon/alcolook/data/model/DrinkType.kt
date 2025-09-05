package com.hackathon.alcolook.data.model

enum class DrinkType {
    SOJU, BEER, WINE, WHISKY, HIGHBALL, COCKTAIL, MAKGEOLLI, OTHER;

    fun getDisplayName(): String = when(this) {
        SOJU -> "소주"
        BEER -> "맥주"
        WINE -> "와인"
        WHISKY -> "위스키"
        HIGHBALL -> "하이볼"
        COCKTAIL -> "칵테일"
        MAKGEOLLI -> "막걸리"
        OTHER -> "기타"
    }

    fun getDefaultAbv(): Float = when(this) {
        SOJU -> 16.9f
        BEER -> 4.5f
        WINE -> 12f
        WHISKY -> 40f
        HIGHBALL -> 5f
        COCKTAIL -> 15f
        MAKGEOLLI -> 6f
        OTHER -> 0f
    }
    
    fun getEmoji(): String = when(this) {
        SOJU -> "🍶"
        BEER -> "🍺"
        WINE -> "🍷"
        WHISKY -> "🥃"
        HIGHBALL -> "🍹"
        COCKTAIL -> "🍸"
        MAKGEOLLI -> "🥛"
        OTHER -> "⭐"
    }
}