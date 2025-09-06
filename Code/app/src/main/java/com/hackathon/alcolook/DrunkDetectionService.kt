package com.hackathon.alcolook

import android.content.Context
import android.graphics.Bitmap
import android.util.Log
import com.amazonaws.services.rekognition.model.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.nio.ByteBuffer

data class FaceBox(
    val left: Float,
    val top: Float,
    val width: Float,
    val height: Float,
    val drunkPercentage: Int,
    val personId: String
)

data class DrunkDetectionResult(
    val drunkPercentage: Float,
    val message: String,
    val faceBoxes: List<FaceBox>
)

class DrunkDetectionService(private val context: Context) {
    
    companion object {
        private const val TAG = "DrunkDetection"
    }
    
    suspend fun detectDrunkLevel(bitmap: Bitmap, isRealTime: Boolean = true): DrunkDetectionResult = withContext(Dispatchers.IO) {
        val rekognitionClient = AwsConfig.getRekognitionClient(context)
        
        Log.d(TAG, "=== ${if (isRealTime) "실시간" else "사진"} 분석 시작 ===")
        Log.d(TAG, "테스트 모드: ${AwsConfig.TEST_MODE}")
        
        return@withContext if (AwsConfig.TEST_MODE || rekognitionClient == null) {
            val testResult = generateTestDrunkLevel()
            val testFaceBox = FaceBox(
                left = 0.25f, 
                top = 0.3f, 
                width = 0.5f, 
                height = 0.4f, 
                drunkPercentage = testResult, 
                personId = "face_1"
            )
            Log.d(TAG, "테스트 모드 결과: ${testResult}%")
            DrunkDetectionResult(testResult.toFloat(), getDrunkMessage(testResult), listOf(testFaceBox))
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
                        val drunkLevel = calculateDrunkPercentage(faceDetail, isRealTime)
                        
                        val boundingBox = faceDetail.boundingBox
                        FaceBox(
                            left = boundingBox.left,
                            top = boundingBox.top,
                            width = boundingBox.width,
                            height = boundingBox.height,
                            drunkPercentage = drunkLevel,
                            personId = "face_${index + 1}"
                        )
                    }
                    
                    val avgDrunkLevel = faces.map { it.drunkPercentage }.average().toFloat()
                    Log.d(TAG, "분석 완료 - 평균 음주도: ${avgDrunkLevel}%")
                    DrunkDetectionResult(avgDrunkLevel, getDrunkMessage(avgDrunkLevel.toInt()), faces)
                } else {
                    Log.d(TAG, "얼굴이 감지되지 않음 - 0% 반환")
                    DrunkDetectionResult(0f, "얼굴이 감지되지 않았습니다", emptyList())
                }
            } catch (e: Exception) {
                Log.e(TAG, "분석 중 오류 발생", e)
                DrunkDetectionResult(0f, "분석 중 오류가 발생했습니다", emptyList())
            }
        }
    }
    
    private fun calculateDrunkPercentage(faceDetail: FaceDetail, isRealTime: Boolean = true): Int {
        var drunkScore = 0f
        val modeText = if (isRealTime) "실시간" else "사진"
        Log.d(TAG, "--- ${modeText} 얼굴 특징 분석 ---")
        
        // 기본 분석
        faceDetail.eyesOpen?.let { eyesOpen ->
            if (!eyesOpen.value && eyesOpen.confidence > 70f) {
                drunkScore += 30f
            }
        }
        
        faceDetail.mouthOpen?.let { mouthOpen ->
            if (mouthOpen.value && mouthOpen.confidence > 75f) {
                drunkScore += 20f
            }
        }
        
        // 모드별 민감도 조정
        val adjustedScore = if (isRealTime) {
            // 실시간: 3배 낮게 (0.33배)
            drunkScore * 0.33f
        } else {
            // 사진: 더 예민하게 (1.5배)
            drunkScore * 1.5f
        }
        
        val finalScore = minOf(100, maxOf(0, adjustedScore.toInt()))
        Log.d(TAG, "${modeText} 음주도: ${finalScore}% (원본: ${drunkScore.toInt()}%)")
        return finalScore
    }
    
    private fun bitmapToByteBuffer(bitmap: Bitmap): ByteBuffer {
        val stream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 80, stream)
        val byteArray = stream.toByteArray()
        return ByteBuffer.wrap(byteArray)
    }
    
    private fun generateTestDrunkLevel(): Int {
        return (0..100).random()
    }
    
    private fun getDrunkMessage(percentage: Int): String {
        return when {
            percentage < 20 -> "정상 상태입니다"
            percentage < 40 -> "약간 취한 상태입니다"
            percentage < 60 -> "취한 상태입니다"
            else -> "많이 취한 상태입니다"
        }
    }
}
