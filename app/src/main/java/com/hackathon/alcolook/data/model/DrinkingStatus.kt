package com.hackathon.alcolook.data.model

enum class DrinkingStatus {
    APPROPRIATE,  // 적정 (남 28g / 여 14g 이내)
    CAUTION,      // 주의 (남 56g / 여 42g 이내 or 주간 기준 내)
    EXCESSIVE,    // 과음 (남 70g / 여 56g)
    DANGEROUS     // 위험 (월 5회 이상 과음)
}