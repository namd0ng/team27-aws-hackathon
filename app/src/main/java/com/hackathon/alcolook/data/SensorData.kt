package com.hackathon.alcolook.data

import java.time.LocalDateTime

/**
 * 얼굴 인식 분석 결과
 */
data class FaceAnalysisData(
    val confidence: Float, // 0.0 ~ 1.0
    val eyesClosed: Boolean,
    val mouthOpen: Boolean,
    val faceAngle: Float, // 기울기 각도
    val timestamp: LocalDateTime = LocalDateTime.now()
)

/**
 * 심박수 측정 데이터
 */
data class HeartRateData(
    val bpm: Int, // 분당 심박수
    val variability: Float, // 심박 변이도
    val measurementDuration: Int, // 측정 시간(초)
    val timestamp: LocalDateTime = LocalDateTime.now()
)

/**
 * 자이로센서 흔들림 데이터
 */
data class GyroscopeData(
    val shakingIntensity: Float, // 흔들림 강도 (0.0 ~ 1.0)
    val averageMovement: Float, // 평균 움직임
    val peakMovement: Float, // 최대 움직임
    val stabilityScore: Float, // 안정성 점수 (0.0 ~ 1.0, 높을수록 안정)
    val timestamp: LocalDateTime = LocalDateTime.now()
)

/**
 * 통합 센서 분석 결과
 */
data class IntegratedSensorData(
    val faceAnalysis: FaceAnalysisData,
    val heartRate: HeartRateData,
    val gyroscope: GyroscopeData,
    val timestamp: LocalDateTime = LocalDateTime.now()
)

/**
 * 최종 음주 상태 결과
 */
enum class IntoxicationLevel {
    NORMAL,      // 정상
    SLIGHTLY,    // 조금 취함
    MODERATE,    // 적당히 취함
    HEAVY        // 과음
}

/**
 * AI 분석 보고서
 */
data class IntoxicationReport(
    val level: IntoxicationLevel,
    val confidence: Float, // 신뢰도 (0.0 ~ 1.0)
    val summary: String, // AI 생성 요약
    val detailedAnalysis: String, // 상세 분석
    val recommendations: List<String>, // 권장사항
    val sensorData: IntegratedSensorData,
    val timestamp: LocalDateTime = LocalDateTime.now()
)
