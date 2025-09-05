package com.hackathon.alcolook

import android.graphics.Bitmap
import com.amazonaws.services.rekognition.model.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.nio.ByteBuffer

class DrunkDetectionService {
    
    private val rekognitionClient = AwsConfig.getRekognitionClient()
    
    suspend fun detectDrunkLevel(bitmap: Bitmap): Int = withContext(Dispatchers.IO) {
        return@withContext if (AwsConfig.TEST_MODE || rekognitionClient == null) {
            // 테스트 모드: 랜덤 값 반환 (실제 카메라 테스트 가능)
            generateTestDrunkLevel()
        } else {
            try {
                val imageBytes = bitmapToByteBuffer(bitmap)
                val image = Image().withBytes(imageBytes)
                
                val request = DetectFacesRequest()
                    .withImage(image)
                    .withAttributes("ALL")
                
                val result = rekognitionClient.detectFaces(request)
                
                if (result.faceDetails.isNotEmpty()) {
                    calculateDrunkPercentage(result.faceDetails[0])
                } else {
                    0 // 얼굴이 감지되지 않음
                }
            } catch (e: Exception) {
                // AWS 연결 실패 시 테스트 값 반환
                generateTestDrunkLevel()
            }
        }
    }
    
    private fun generateTestDrunkLevel(): Int {
        // 테스트용 랜덤 값 (20-80% 범위)
        return (20..80).random()
    }
    
    private fun calculateDrunkPercentage(faceDetail: FaceDetail): Int {
        var drunkScore = 0f
        
        // 눈 상태 분석 (눈이 감겨있거나 반쯤 감긴 상태)
        faceDetail.eyesOpen?.let { eyesOpen ->
            if (!eyesOpen.value) {
                drunkScore += 30f
            } else if (eyesOpen.confidence < 70f) {
                drunkScore += 15f
            }
        }
        
        // 입 상태 분석 (입이 벌어져 있는 상태)
        faceDetail.mouthOpen?.let { mouthOpen ->
            if (mouthOpen.value && mouthOpen.confidence > 70f) {
                drunkScore += 20f
            }
        }
        
        // 표정 분석
        faceDetail.emotions?.forEach { emotion ->
            when (emotion.type) {
                "CONFUSED" -> drunkScore += emotion.confidence * 0.3f
                "SURPRISED" -> drunkScore += emotion.confidence * 0.2f
                "DISGUSTED" -> drunkScore += emotion.confidence * 0.25f
            }
        }
        
        // 얼굴 기울기 분석
        faceDetail.pose?.let { pose ->
            val rollAngle = Math.abs(pose.roll ?: 0f)
            val pitchAngle = Math.abs(pose.pitch ?: 0f)
            val yawAngle = Math.abs(pose.yaw ?: 0f)
            
            if (rollAngle > 15f) drunkScore += 15f
            if (pitchAngle > 20f) drunkScore += 10f
            if (yawAngle > 25f) drunkScore += 10f
        }
        
        // 0-100% 범위로 정규화
        return minOf(100, maxOf(0, drunkScore.toInt()))
    }
    
    private fun bitmapToByteBuffer(bitmap: Bitmap): ByteBuffer {
        val stream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 80, stream)
        val byteArray = stream.toByteArray()
        return ByteBuffer.wrap(byteArray)
    }
}
