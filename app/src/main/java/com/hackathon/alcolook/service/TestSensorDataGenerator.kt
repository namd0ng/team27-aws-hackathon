package com.hackathon.alcolook.service

import com.hackathon.alcolook.data.*
import kotlin.random.Random

object TestSensorDataGenerator {
    
    /**
     * 테스트용 통합 센서 데이터 생성
     * @param intoxicationLevel 원하는 음주 상태 (null이면 랜덤)
     */
    fun generateTestData(intoxicationLevel: IntoxicationLevel? = null): IntegratedSensorData {
        val targetLevel = intoxicationLevel ?: IntoxicationLevel.values().random()
        
        return IntegratedSensorData(
            faceAnalysis = generateFaceData(targetLevel),
            heartRate = generateHeartRateData(targetLevel),
            gyroscope = generateGyroscopeData(targetLevel)
        )
    }
    
    private fun generateFaceData(level: IntoxicationLevel): FaceAnalysisData {
        return when (level) {
            IntoxicationLevel.NORMAL -> FaceAnalysisData(
                confidence = Random.nextFloat() * 0.2f + 0.8f, // 0.8 ~ 1.0
                eyesClosed = Random.nextFloat() < 0.1f, // 10% 확률
                mouthOpen = Random.nextFloat() < 0.2f, // 20% 확률
                faceAngle = Random.nextFloat() * 10f - 5f // -5 ~ 5도
            )
            IntoxicationLevel.SLIGHTLY -> FaceAnalysisData(
                confidence = Random.nextFloat() * 0.3f + 0.6f, // 0.6 ~ 0.9
                eyesClosed = Random.nextFloat() < 0.3f, // 30% 확률
                mouthOpen = Random.nextFloat() < 0.4f, // 40% 확률
                faceAngle = Random.nextFloat() * 20f - 10f // -10 ~ 10도
            )
            IntoxicationLevel.MODERATE -> FaceAnalysisData(
                confidence = Random.nextFloat() * 0.3f + 0.4f, // 0.4 ~ 0.7
                eyesClosed = Random.nextFloat() < 0.5f, // 50% 확률
                mouthOpen = Random.nextFloat() < 0.6f, // 60% 확률
                faceAngle = Random.nextFloat() * 30f - 15f // -15 ~ 15도
            )
            IntoxicationLevel.HEAVY -> FaceAnalysisData(
                confidence = Random.nextFloat() * 0.4f + 0.2f, // 0.2 ~ 0.6
                eyesClosed = Random.nextFloat() < 0.7f, // 70% 확률
                mouthOpen = Random.nextFloat() < 0.8f, // 80% 확률
                faceAngle = Random.nextFloat() * 40f - 20f // -20 ~ 20도
            )
        }
    }
    
    private fun generateHeartRateData(level: IntoxicationLevel): HeartRateData {
        return when (level) {
            IntoxicationLevel.NORMAL -> HeartRateData(
                bpm = Random.nextInt(60, 90), // 정상 심박수
                variability = Random.nextFloat() * 0.1f + 0.05f, // 낮은 변이도
                measurementDuration = Random.nextInt(10, 30)
            )
            IntoxicationLevel.SLIGHTLY -> HeartRateData(
                bpm = Random.nextInt(85, 110), // 약간 높은 심박수
                variability = Random.nextFloat() * 0.15f + 0.1f,
                measurementDuration = Random.nextInt(10, 30)
            )
            IntoxicationLevel.MODERATE -> HeartRateData(
                bpm = Random.nextInt(100, 130), // 높은 심박수
                variability = Random.nextFloat() * 0.2f + 0.15f,
                measurementDuration = Random.nextInt(10, 30)
            )
            IntoxicationLevel.HEAVY -> HeartRateData(
                bpm = Random.nextInt(120, 160), // 매우 높은 심박수
                variability = Random.nextFloat() * 0.3f + 0.2f,
                measurementDuration = Random.nextInt(10, 30)
            )
        }
    }
    
    private fun generateGyroscopeData(level: IntoxicationLevel): GyroscopeData {
        return when (level) {
            IntoxicationLevel.NORMAL -> GyroscopeData(
                shakingIntensity = Random.nextFloat() * 0.2f, // 0.0 ~ 0.2
                averageMovement = Random.nextFloat() * 0.1f,
                peakMovement = Random.nextFloat() * 0.3f,
                stabilityScore = Random.nextFloat() * 0.2f + 0.8f // 0.8 ~ 1.0
            )
            IntoxicationLevel.SLIGHTLY -> GyroscopeData(
                shakingIntensity = Random.nextFloat() * 0.3f + 0.2f, // 0.2 ~ 0.5
                averageMovement = Random.nextFloat() * 0.2f + 0.1f,
                peakMovement = Random.nextFloat() * 0.4f + 0.3f,
                stabilityScore = Random.nextFloat() * 0.3f + 0.5f // 0.5 ~ 0.8
            )
            IntoxicationLevel.MODERATE -> GyroscopeData(
                shakingIntensity = Random.nextFloat() * 0.3f + 0.5f, // 0.5 ~ 0.8
                averageMovement = Random.nextFloat() * 0.3f + 0.2f,
                peakMovement = Random.nextFloat() * 0.5f + 0.4f,
                stabilityScore = Random.nextFloat() * 0.3f + 0.2f // 0.2 ~ 0.5
            )
            IntoxicationLevel.HEAVY -> GyroscopeData(
                shakingIntensity = Random.nextFloat() * 0.2f + 0.8f, // 0.8 ~ 1.0
                averageMovement = Random.nextFloat() * 0.4f + 0.3f,
                peakMovement = Random.nextFloat() * 0.6f + 0.4f,
                stabilityScore = Random.nextFloat() * 0.2f // 0.0 ~ 0.2
            )
        }
    }
    
    /**
     * 여러 개의 테스트 데이터 생성
     */
    fun generateMultipleTestData(count: Int): List<IntegratedSensorData> {
        return (1..count).map { generateTestData() }
    }
}
