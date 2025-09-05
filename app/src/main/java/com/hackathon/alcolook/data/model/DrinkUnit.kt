package com.hackathon.alcolook.data.model

enum class DrinkUnit {
    GLASS, BOTTLE, CAN, SHOT, OTHER;

    fun getDisplayName(): String = when(this) {
        GLASS -> "잔"
        BOTTLE -> "병"
        CAN -> "캔"
        SHOT -> "샷"
        OTHER -> "기타"
    }

    fun getVolumeMl(drinkType: DrinkType): Int = when(this) {
        GLASS -> when(drinkType) {
            DrinkType.SOJU -> 50
            DrinkType.BEER -> 200
            DrinkType.WINE -> 150
            DrinkType.WHISKY -> 30
            else -> 50
        }
        BOTTLE -> when(drinkType) {
            DrinkType.SOJU -> 360
            DrinkType.BEER -> 500
            DrinkType.WINE -> 750
            else -> 360
        }
        CAN -> when(drinkType) {
            DrinkType.BEER -> 355
            DrinkType.HIGHBALL -> 350
            else -> 355
        }
        SHOT -> 30
        OTHER -> 0
    }
}