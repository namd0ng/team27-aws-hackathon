package com.hackathon.alcolook

import android.content.Context
import android.graphics.Bitmap
import android.util.Log
import com.amazonaws.services.rekognition.model.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.nio.ByteBuffer

data class FaceAnalysisResult(
    val faces: List<FaceBox>
)

data class FaceBox(
    val left: Float,
    val top: Float,
    val width: Float,
    val height: Float,
    val drunkPercentage: Int,
    val personId: String
)

class DrunkDetectionService(private val context: Context) {
    
    companion object {
        private const val TAG = "DrunkDetection"
    }
    
    suspend fun detectDrunkLevel(bitmap: Bitmap): FaceAnalysisResult = withContext(Dispatchers.IO) {
        val rekognitionClient = AwsConfig.getRekognitionClient(context)
        
        Log.d(TAG, "=== 얼굴 분석 시작 ===")
        Log.d(TAG, "테스트 모드: ${AwsConfig.TEST_MODE}")
        
        return@withContext if (AwsConfig.TEST_MODE || rekognitionClient == null) {
            val testResult = generateTestDrunkLevel()
            val testFaces = listOf(
                FaceBox(0.2f, 0.2f, 0.3f, 0.4f, testResult, "Person 1"),
                FaceBox(0.5f, 0.3f, 0.3f, 0.4f, generateTestDrunkLevel(), "Person 2")
            )
            Log.d(TAG, "테스트 모드 결과: ${testResult}%")
            FaceAnalysisResult(testFaces)
        } else {
            try {
                Log.d(TAG, "AWS Rekognition 분석 중...")
                val imageBytes = bitmapToByteBuffer(bitmap)
                val image = Image().withBytes(imageBytes)
                
                val request = DetectFacesRequest()
                    .withImage(image)
                    .withAttributes("ALL")
                
                val result = rekognitionClient.detectFaces(request)
                
                if (result.faceDetails.isNotEmpty()) {
                    val faces = result.faceDetails.mapIndexed { index, faceDetail ->
                        val drunkLevel = calculateDrunkPercentage(faceDetail)
                        
                        // 얼굴 박스 정보 추출 (더 정확한 좌표)
                        val boundingBox = faceDetail.boundingBox
                        FaceBox(
                            left = boundingBox.left,
                            top = boundingBox.top,
                            width = boundingBox.width,
                            height = boundingBox.height,
                            drunkPercentage = drunkLevel,
                            personId = "Person ${index + 1}"
                        )
                    }
                    
                    Log.d(TAG, "실시간 분석 완료 - ${faces.size}명 감지")
                    faces.forEach { face ->
                        Log.d(TAG, "${face.personId}: ${face.drunkPercentage}%")
                    }
                    FaceAnalysisResult(faces)
                } else {
                    Log.d(TAG, "얼굴이 감지되지 않음")
                    FaceAnalysisResult(emptyList())
                }
            } catch (e: Exception) {
                Log.e(TAG, "AWS 연결 실패: ${e.message}")
                val fallbackResult = generateTestDrunkLevel()
                val testFaces = listOf(
                    FaceBox(0.2f, 0.2f, 0.3f, 0.4f, fallbackResult, "Person 1")
                )
                Log.d(TAG, "대체 결과: ${fallbackResult}%")
                FaceAnalysisResult(testFaces)
            }
        }
    }
    
    private fun generateTestDrunkLevel(): Int {
        return (0..30).random()
    }
    
    private fun calculateDrunkPercentage(faceDetail: FaceDetail): Int {
        var drunkScore = 0f
        Log.d(TAG, "--- 얼굴 특징 분석 ---")
        
        // 눈 상태 분석 (실시간용 - 더 보수적)
        faceDetail.eyesOpen?.let { eyesOpen ->
            Log.d(TAG, "눈 상태: ${if (eyesOpen.value) "열림" else "감김"} (신뢰도: ${eyesOpen.confidence}%)")
            if (!eyesOpen.value && eyesOpen.confidence > 85f) {
                drunkScore += 30f
                Log.d(TAG, "눈 완전히 감김으로 +30점")
            } else if (eyesOpen.confidence < 40f) {
                drunkScore += 15f
                Log.d(TAG, "눈 상태 매우 불확실로 +15점")
            }
        }
        
        // 입 상태 분석 (실시간용 - 더 보수적)
        faceDetail.mouthOpen?.let { mouthOpen ->
            Log.d(TAG, "입 상태: ${if (mouthOpen.value) "열림" else "닫힘"} (신뢰도: ${mouthOpen.confidence}%)")
            if (mouthOpen.value && mouthOpen.confidence > 90f) {
                drunkScore += 20f
                Log.d(TAG, "입 크게 벌림으로 +20점")
            }
        }
        
        // 표정 분석 (높은 임계값 + 여러 표정 조합 필요)
        Log.d(TAG, "감정 분석:")
        var negativeEmotionCount = 0
        var totalNegativeConfidence = 0f
        
        faceDetail.emotions?.forEach { emotion ->
            Log.d(TAG, "  ${emotion.type}: ${emotion.confidence}%")
            when (emotion.type) {
                "CONFUSED" -> {
                    if (emotion.confidence > 80f) {
                        val score = emotion.confidence * 0.25f
                        drunkScore += score
                        negativeEmotionCount++
                        totalNegativeConfidence += emotion.confidence
                        Log.d(TAG, "  강한 혼란 표정으로 +${score}점")
                    }
                }
                "DISGUSTED" -> {
                    if (emotion.confidence > 85f) {
                        val score = emotion.confidence * 0.2f
                        drunkScore += score
                        negativeEmotionCount++
                        totalNegativeConfidence += emotion.confidence
                        Log.d(TAG, "  강한 혐오 표정으로 +${score}점")
                    }
                }
                "SAD" -> {
                    if (emotion.confidence > 75f) {
                        val score = emotion.confidence * 0.15f
                        drunkScore += score
                        negativeEmotionCount++
                        totalNegativeConfidence += emotion.confidence
                        Log.d(TAG, "  슬픔 표정으로 +${score}점")
                    }
                }
            }
        }
        
        // 여러 부정적 감정이 동시에 나타날 때만 추가 점수
        if (negativeEmotionCount >= 2) {
            val bonus = (totalNegativeConfidence / negativeEmotionCount) * 0.1f
            drunkScore += bonus
            Log.d(TAG, "복합 부정감정 보너스: +${bonus}점")
        }
        
        // 얼굴 기울기 분석 (더 관대한 기준)
        faceDetail.pose?.let { pose ->
            val rollAngle = Math.abs(pose.roll ?: 0f)
            val pitchAngle = Math.abs(pose.pitch ?: 0f)
            val yawAngle = Math.abs(pose.yaw ?: 0f)
            
            Log.d(TAG, "얼굴 기울기: Roll=${rollAngle}°, Pitch=${pitchAngle}°, Yaw=${yawAngle}°")
            
            // 심각한 기울기만 감지
            if (rollAngle > 30f) {
                drunkScore += 15f
                Log.d(TAG, "심한 Roll 기울기로 +15점")
            }
            if (pitchAngle > 35f) {
                drunkScore += 10f
                Log.d(TAG, "심한 Pitch 기울기로 +10점")
            }
            if (yawAngle > 40f) {
                drunkScore += 10f
                Log.d(TAG, "심한 Yaw 기울기로 +10점")
            }
        }
        
        // 실시간용: 증폭 없이 원점수 사용, 최소 임계값 적용
        val finalScore = if (drunkScore < 25f) {
            // 25점 미만은 0으로 처리 (노이즈 제거)
            0
        } else {
            minOf(100, maxOf(0, drunkScore.toInt()))
        }
        
        Log.d(TAG, "최종 음주도: ${finalScore}% (원점수: ${drunkScore})")
        Log.d(TAG, "===================")
        
        return finalScore
    }
    
    private fun bitmapToByteBuffer(bitmap: Bitmap): ByteBuffer {
        val stream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 80, stream)
        val byteArray = stream.toByteArray()
        return ByteBuffer.wrap(byteArray)
    }
}
