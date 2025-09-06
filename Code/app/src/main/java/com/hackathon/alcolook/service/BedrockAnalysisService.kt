package com.hackathon.alcolook.service

import android.content.Context
import android.graphics.Bitmap
import com.hackathon.alcolook.AwsConfig
import com.hackathon.alcolook.DrunkDetectionService
import com.hackathon.alcolook.data.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.IOException
import java.util.*

class BedrockAnalysisService(private val context: Context) {
    
    private val client = OkHttpClient()
    private val drunkDetectionService = DrunkDetectionService(context)
    
    suspend fun analyzeIntoxication(
        bitmap: Bitmap? = null,
        sensorData: IntegratedSensorData? = null
    ): IntoxicationReport {
        return withContext(Dispatchers.IO) {
            val finalSensorData = sensorData ?: generateSensorDataWithFaceAnalysis(bitmap)
            
            if (AwsConfig.TEST_MODE) {
                generateTestReport(finalSensorData)
            } else {
                generateRealReport(finalSensorData)
            }
        }
    }
    
    private suspend fun generateSensorDataWithFaceAnalysis(bitmap: Bitmap?): IntegratedSensorData {
        val faceAnalysis = if (bitmap != null) {
            // 기존 얼굴 인식 로직 사용
            val result = drunkDetectionService.detectDrunkLevel(bitmap)
            FaceAnalysisData(
                confidence = result.drunkPercentage / 100f,
                eyesClosed = result.drunkPercentage > 50,
                mouthOpen = result.drunkPercentage > 40,
                faceAngle = (result.drunkPercentage - 50) * 0.5f
            )
        } else {
            // 테스트 데이터 생성
            TestSensorDataGenerator.generateTestData().faceAnalysis
        }
        
        return IntegratedSensorData(
            faceAnalysis = faceAnalysis,
            heartRate = TestSensorDataGenerator.generateTestData().heartRate,
            gyroscope = TestSensorDataGenerator.generateTestData().gyroscope
        )
    }
    
    private suspend fun generateRealReport(sensorData: IntegratedSensorData): IntoxicationReport {
        return try {
            // AWS 자격증명 로드
            val properties = Properties()
            context.assets.open("aws-credentials.properties").use { input ->
                properties.load(input)
            }
            
            val accessKey = properties.getProperty("aws.access.key")
            val secretKey = properties.getProperty("aws.secret.key")
            
            if (accessKey.isNullOrEmpty() || secretKey.isNullOrEmpty()) {
                return generateTestReport(sensorData)
            }
            
            val prompt = buildPrompt(sensorData)
            val requestBody = JSONObject().apply {
                put("anthropic_version", "bedrock-2023-05-31")
                put("max_tokens", 1000)
                put("messages", arrayOf(
                    JSONObject().apply {
                        put("role", "user")
                        put("content", prompt)
                    }
                ))
            }
            
            val region = "us-east-1"
            val service = "bedrock"
            val host = "bedrock-runtime.$region.amazonaws.com"
            val uri = "/model/anthropic.claude-3-opus-20240229-v1:0/invoke"
            
            val headers = mapOf(
                "Host" to host,
                "Content-Type" to "application/json",
                "X-Amz-Target" to "AmazonBedrockRuntime.InvokeModel"
            )
            
            val authorization = AwsSignatureV4.sign(
                accessKey = accessKey,
                secretKey = secretKey,
                region = region,
                service = service,
                method = "POST",
                uri = uri,
                queryString = "",
                headers = headers,
                payload = requestBody.toString()
            )
            
            val request = Request.Builder()
                .url("https://$host$uri")
                .post(requestBody.toString().toRequestBody("application/json".toMediaType()))
                .header("Authorization", authorization)
                .header("Host", host)
                .header("Content-Type", "application/json")
                .header("X-Amz-Target", "AmazonBedrockRuntime.InvokeModel")
                .build()
            
            val response = client.newCall(request).execute()
            val responseBody = response.body?.string() ?: ""
            
            if (response.isSuccessful) {
                parseClaudeResponse(responseBody, sensorData)
            } else {
                generateTestReport(sensorData)
            }
            
        } catch (e: Exception) {
            generateTestReport(sensorData)
        }
    }
    
    private fun parseClaudeResponse(response: String, sensorData: IntegratedSensorData): IntoxicationReport {
        return try {
            val responseJson = JSONObject(response)
            val content = responseJson.getJSONArray("content").getJSONObject(0).getString("text")
            
            // JSON 부분만 추출
            val jsonStart = content.indexOf("{")
            val jsonEnd = content.lastIndexOf("}") + 1
            val jsonContent = content.substring(jsonStart, jsonEnd)
            
            val analysisJson = JSONObject(jsonContent)
            
            IntoxicationReport(
                level = IntoxicationLevel.valueOf(analysisJson.getString("level")),
                confidence = analysisJson.getDouble("confidence").toFloat(),
                summary = analysisJson.getString("summary"),
                detailedAnalysis = analysisJson.getString("detailed_analysis"),
                recommendations = analysisJson.getJSONArray("recommendations").let { array ->
                    (0 until array.length()).map { array.getString(it) }
                },
                sensorData = sensorData
            )
        } catch (e: Exception) {
            generateTestReport(sensorData)
        }
    }
    
    private fun buildPrompt(sensorData: IntegratedSensorData): String {
        return """
음주 상태 분석을 위한 센서 데이터를 제공합니다. 다음 데이터를 종합하여 음주 상태를 분석하고 보고서를 작성해주세요.

**얼굴 인식 데이터:**
- 음주 확률: ${(sensorData.faceAnalysis.confidence * 100).toInt()}%
- 눈 감김 여부: ${sensorData.faceAnalysis.eyesClosed}
- 입 벌림 여부: ${sensorData.faceAnalysis.mouthOpen}
- 얼굴 기울기: ${sensorData.faceAnalysis.faceAngle}도

**심박수 데이터:**
- 심박수: ${sensorData.heartRate.bpm} BPM
- 심박 변이도: ${sensorData.heartRate.variability}
- 측정 시간: ${sensorData.heartRate.measurementDuration}초

**자이로센서 데이터:**
- 흔들림 강도: ${sensorData.gyroscope.shakingIntensity}
- 평균 움직임: ${sensorData.gyroscope.averageMovement}
- 최대 움직임: ${sensorData.gyroscope.peakMovement}
- 안정성 점수: ${sensorData.gyroscope.stabilityScore}

다음 형식으로 JSON 응답을 제공해주세요:
{
  "level": "NORMAL|SLIGHTLY|MODERATE|HEAVY",
  "confidence": 0.0-1.0,
  "summary": "간단한 요약 (1-2문장)",
  "detailed_analysis": "상세 분석 (3-4문장)",
  "recommendations": ["권장사항1", "권장사항2", "권장사항3"]
}
        """.trimIndent()
    }
    
    private fun generateTestReport(sensorData: IntegratedSensorData): IntoxicationReport {
        // 기존 얼굴 인식 결과를 주요 지표로 사용
        val faceScore = sensorData.faceAnalysis.confidence * 100 // 0-100점
        val heartScore = calculateHeartScore(sensorData.heartRate)
        val gyroScore = calculateGyroScore(sensorData.gyroscope)
        
        // 가중 평균 (얼굴 60%, 심박 20%, 자이로 20%)
        val totalScore = (faceScore * 0.6f + heartScore * 0.2f + gyroScore * 0.2f).toInt()
        
        val level = when (totalScore) {
            in 80..100 -> IntoxicationLevel.NORMAL
            in 60..79 -> IntoxicationLevel.SLIGHTLY
            in 40..59 -> IntoxicationLevel.MODERATE
            else -> IntoxicationLevel.HEAVY
        }
        
        val levelText = when (level) {
            IntoxicationLevel.NORMAL -> "정상"
            IntoxicationLevel.SLIGHTLY -> "조금 취함"
            IntoxicationLevel.MODERATE -> "적당히 취함"
            IntoxicationLevel.HEAVY -> "과음"
        }
        
        return IntoxicationReport(
            level = level,
            confidence = totalScore / 100f,
            summary = "종합 점수 ${totalScore}점으로 '$levelText' 상태입니다.",
            detailedAnalysis = buildDetailedAnalysis(faceScore, heartScore, gyroScore, totalScore, sensorData),
            recommendations = getRecommendations(level, totalScore),
            sensorData = sensorData
        )
    }
    
    private fun calculateHeartScore(heart: HeartRateData): Float {
        val bpm = heart.bpm
        val baseScore = when {
            bpm in 60..90 -> 100f
            bpm in 50..110 -> 80f
            bpm in 40..130 -> 60f
            else -> 30f
        }
        
        val variabilityPenalty = heart.variability * 50
        return (baseScore - variabilityPenalty).coerceIn(0f, 100f)
    }
    
    private fun calculateGyroScore(gyro: GyroscopeData): Float {
        val stabilityScore = gyro.stabilityScore * 100
        val shakingPenalty = gyro.shakingIntensity * 30
        val movementPenalty = gyro.peakMovement * 20
        
        return (stabilityScore - shakingPenalty - movementPenalty).coerceIn(0f, 100f)
    }
    
    private fun buildDetailedAnalysis(
        faceScore: Float, 
        heartScore: Float, 
        gyroScore: Float, 
        totalScore: Int,
        sensorData: IntegratedSensorData
    ): String {
        return """
📊 상세 분석 결과

• 얼굴 분석: ${faceScore.toInt()}점 (가중치 60%)
  - 음주 확률: ${(sensorData.faceAnalysis.confidence * 100).toInt()}%
  - 눈 상태: ${if (sensorData.faceAnalysis.eyesClosed) "감김" else "정상"}
  - 입 상태: ${if (sensorData.faceAnalysis.mouthOpen) "벌림" else "정상"}

• 심박수 분석: ${heartScore.toInt()}점 (가중치 20%)
  - 심박수: ${sensorData.heartRate.bpm} BPM ${getHeartRateStatus(sensorData.heartRate.bpm)}

• 움직임 분석: ${gyroScore.toInt()}점 (가중치 20%)
  - 안정성: ${(sensorData.gyroscope.stabilityScore * 100).toInt()}%

🎯 최종 점수: ${totalScore}점
        """.trimIndent()
    }
    
    private fun getHeartRateStatus(bpm: Int): String = when {
        bpm in 60..90 -> "(정상)"
        bpm in 50..110 -> "(약간 높음)"
        bpm > 110 -> "(높음)"
        else -> "(낮음)"
    }
    
    private fun getRecommendations(level: IntoxicationLevel, score: Int): List<String> {
        return when (level) {
            IntoxicationLevel.NORMAL -> listOf(
                "현재 상태가 양호합니다 (${score}점)",
                "적당한 수분 섭취를 권장합니다",
                "안전한 귀가를 위해 대중교통을 이용하세요"
            )
            IntoxicationLevel.SLIGHTLY -> listOf(
                "주의가 필요한 상태입니다 (${score}점)",
                "충분한 휴식을 취하세요",
                "물을 많이 마시고 운전은 피하세요"
            )
            IntoxicationLevel.MODERATE -> listOf(
                "위험한 상태입니다 (${score}점)",
                "즉시 음주를 중단하세요",
                "안전한 장소에서 휴식하고 동행자와 함께 있으세요"
            )
            IntoxicationLevel.HEAVY -> listOf(
                "매우 위험한 상태입니다 (${score}점)",
                "즉시 의료진의 도움을 받으세요",
                "혼자 있지 말고 응급상황에 대비하세요"
            )
        }
    }
}

// 이게 최신 제일 최신이에요 ㅠㅠㅠㅠ