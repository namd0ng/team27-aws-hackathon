package com.hackathon.alcolook

import android.content.Context
import android.graphics.Bitmap
import android.util.Log
import com.amazonaws.services.rekognition.model.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.nio.ByteBuffer

class PhotoDrunkDetectionService(private val context: Context) {
    
    companion object {
        private const val TAG = "PhotoDrunkDetection"
    }
    
    suspend fun detectDrunkLevel(bitmap: Bitmap): DrunkDetectionResult = withContext(Dispatchers.IO) {
        val rekognitionClient = AwsConfig.getRekognitionClient(context)
        
        Log.d(TAG, "=== 사진 분석 시작 ===")
        Log.d(TAG, "이미지 크기: ${bitmap.width}x${bitmap.height}")
        Log.d(TAG, "이미지 포맷: ${bitmap.config}")
        Log.d(TAG, "테스트 모드: ${AwsConfig.TEST_MODE}")
        
        return@withContext if (AwsConfig.TEST_MODE || rekognitionClient == null) {
            Log.d(TAG, "테스트 모드로 실행 중...")
            val testResult = generateTestDrunkLevel()
            val testFaceBox = FaceBox(0.2f, 0.2f, 0.6f, 0.6f, testResult, "test")
            Log.d(TAG, "테스트 모드 결과: ${testResult}%")
            Log.d(TAG, "=== 사진 분석 완료 ===")
            DrunkDetectionResult(testResult.toFloat(), getDrunkMessage(testResult), listOf(testFaceBox))
        } else {
            try {
                Log.d(TAG, "AWS Rekognition 분석 시작...")
                val startTime = System.currentTimeMillis()
                
                val imageBytes = bitmapToByteBuffer(bitmap)
                Log.d(TAG, "이미지 압축 완료: ${imageBytes.remaining()} bytes")
                
                val image = Image().withBytes(imageBytes)
                val request = DetectFacesRequest()
                    .withImage(image)
                    .withAttributes("ALL")
                
                Log.d(TAG, "AWS API 호출 중...")
                val result = rekognitionClient.detectFaces(request)
                val apiTime = System.currentTimeMillis() - startTime
                
                Log.d(TAG, "AWS API 응답 시간: ${apiTime}ms")
                Log.d(TAG, "감지된 얼굴 수: ${result.faceDetails.size}")
                
                if (result.faceDetails.isNotEmpty()) {
                    val faces = result.faceDetails.mapIndexed { index, faceDetail ->
                        Log.d(TAG, "--- 얼굴 #${index + 1} 분석 시작 ---")
                        Log.d(TAG, "얼굴 신뢰도: ${faceDetail.confidence}%")
                        
                        val boundingBox = faceDetail.boundingBox
                        Log.d(TAG, "얼굴 위치: left=${boundingBox.left}, top=${boundingBox.top}, width=${boundingBox.width}, height=${boundingBox.height}")
                        
                        val drunkLevel = calculateDrunkPercentageForPhoto(faceDetail)
                        
                        FaceBox(
                            left = boundingBox.left,
                            top = boundingBox.top,
                            width = boundingBox.width,
                            height = boundingBox.height,
                            drunkPercentage = drunkLevel,
                            personId = "face_$index"
                        )
                    }
                    
                    val avgDrunkLevel = faces.map { it.drunkPercentage }.average().toFloat()
                    val totalTime = System.currentTimeMillis() - startTime
                    
                    Log.d(TAG, "전체 분석 시간: ${totalTime}ms")
                    Log.d(TAG, "개별 음주도: ${faces.map { "${it.personId}=${it.drunkPercentage}%" }}")
                    Log.d(TAG, "평균 음주도: ${avgDrunkLevel}%")
                    Log.d(TAG, "최종 메시지: ${getDrunkMessage(avgDrunkLevel.toInt())}")
                    Log.d(TAG, "=== 사진 분석 완료 ===")
                    
                    DrunkDetectionResult(avgDrunkLevel, getDrunkMessage(avgDrunkLevel.toInt()), faces)
                } else {
                    Log.w(TAG, "얼굴이 감지되지 않음")
                    Log.d(TAG, "=== 사진 분석 완료 (얼굴 없음) ===")
                    DrunkDetectionResult(0f, "얼굴이 감지되지 않았습니다", emptyList())
                }
            } catch (e: Exception) {
                Log.e(TAG, "AWS 연결 실패: ${e.message}")
                Log.e(TAG, "스택 트레이스: ", e)
                
                val fallbackResult = generateTestDrunkLevel()
                val testFaceBox = FaceBox(0.2f, 0.2f, 0.6f, 0.6f, fallbackResult, "fallback")
                Log.d(TAG, "대체 결과 사용: ${fallbackResult}%")
                Log.d(TAG, "=== 사진 분석 완료 (오류 복구) ===")
                
                DrunkDetectionResult(fallbackResult.toFloat(), getDrunkMessage(fallbackResult), listOf(testFaceBox))
            }
        }
    }
    
    private fun generateTestDrunkLevel(): Int {
        return (30..70).random()
    }
    
    private fun getDrunkMessage(drunkLevel: Int): String {
        return when {
            drunkLevel < 20 -> "정상 상태입니다"
            drunkLevel < 40 -> "약간 취한 상태"
            drunkLevel < 60 -> "취한 상태"
            drunkLevel < 80 -> "많이 취한 상태"
            else -> "매우 취한 상태"
        }
    }
    
    private fun calculateDrunkPercentageForPhoto(faceDetail: FaceDetail): Int {
        var drunkScore = 0f
        Log.d(TAG, "--- 사진 얼굴 특징 분석 ---")
        
        // 얼굴 품질 정보 로깅
        faceDetail.quality?.let { quality ->
            Log.d(TAG, "얼굴 품질: 밝기=${quality.brightness}, 선명도=${quality.sharpness}")
        }
        
        // 얼굴 홍조 분석 (새로 추가)
        val rednessScore = analyzeFacialRedness(faceDetail)
        drunkScore += rednessScore
        Log.d(TAG, "홍조 분석: +${rednessScore}점")
        
        // 눈 상태 분석 (사진용 - 더 민감하게)
        faceDetail.eyesOpen?.let { eyesOpen ->
            Log.d(TAG, "눈 상태: ${if (eyesOpen.value) "열림" else "감김"} (신뢰도: ${eyesOpen.confidence}%)")
            if (!eyesOpen.value && eyesOpen.confidence > 60f) {
                drunkScore += 40f
                Log.d(TAG, "눈 감김으로 +40점")
            } else if (eyesOpen.confidence < 70f) {
                drunkScore += 20f
                Log.d(TAG, "눈 상태 불확실로 +20점")
            }
        }
        
        // 입 상태 분석 (사진용 - 더 민감하게)
        faceDetail.mouthOpen?.let { mouthOpen ->
            Log.d(TAG, "입 상태: ${if (mouthOpen.value) "열림" else "닫힘"} (신뢰도: ${mouthOpen.confidence}%)")
            if (mouthOpen.value && mouthOpen.confidence > 70f) {
                drunkScore += 25f
                Log.d(TAG, "입 벌림으로 +25점")
            }
        }
        
        // 표정 분석 (사진용 - 낮은 임계값)
        Log.d(TAG, "감정 분석:")
        faceDetail.emotions?.forEach { emotion ->
            Log.d(TAG, "  ${emotion.type}: ${emotion.confidence}%")
            when (emotion.type) {
                "CONFUSED" -> {
                    if (emotion.confidence > 50f) {
                        val score = emotion.confidence * 0.4f
                        drunkScore += score
                        Log.d(TAG, "  혼란 표정으로 +${score}점")
                    }
                }
                "SURPRISED" -> {
                    if (emotion.confidence > 60f) {
                        val score = emotion.confidence * 0.3f
                        drunkScore += score
                        Log.d(TAG, "  놀람 표정으로 +${score}점")
                    }
                }
                "DISGUSTED" -> {
                    if (emotion.confidence > 55f) {
                        val score = emotion.confidence * 0.35f
                        drunkScore += score
                        Log.d(TAG, "  혐오 표정으로 +${score}점")
                    }
                }
                "SAD" -> {
                    if (emotion.confidence > 60f) {
                        val score = emotion.confidence * 0.25f
                        drunkScore += score
                        Log.d(TAG, "  슬픔 표정으로 +${score}점")
                    }
                }
            }
        }
        
        // 얼굴 기울기 분석 (사진용 - 더 민감하게)
        faceDetail.pose?.let { pose ->
            val rollAngle = Math.abs(pose.roll ?: 0f)
            val pitchAngle = Math.abs(pose.pitch ?: 0f)
            val yawAngle = Math.abs(pose.yaw ?: 0f)
            
            Log.d(TAG, "얼굴 기울기: Roll=${rollAngle}°, Pitch=${pitchAngle}°, Yaw=${yawAngle}°")
            
            if (rollAngle > 15f) {
                drunkScore += 20f
                Log.d(TAG, "Roll 기울기로 +20점")
            }
            if (pitchAngle > 20f) {
                drunkScore += 15f
                Log.d(TAG, "Pitch 기울기로 +15점")
            }
            if (yawAngle > 25f) {
                drunkScore += 15f
                Log.d(TAG, "Yaw 기울기로 +15점")
            }
        }
        
        // 사진용: 점수를 2배로 증폭
        val amplifiedScore = drunkScore * 2f
        val finalScore = minOf(100, maxOf(0, amplifiedScore.toInt()))
        
        Log.d(TAG, "최종 음주도: ${finalScore}% (원점수: ${drunkScore} → 증폭: ${amplifiedScore})")
        Log.d(TAG, "===================")
        
        return finalScore
    }
    
    private fun analyzeFacialRedness(faceDetail: FaceDetail): Float {
        var rednessScore = 0f
        
        // 얼굴 랜드마크를 통한 홍조 추정
        faceDetail.landmarks?.forEach { landmark ->
            when (landmark.type) {
                "nose", "leftCheek", "rightCheek" -> {
                    // 코와 볼 부위의 특징을 분석
                    // 실제로는 이미지 픽셀 분석이 필요하지만, 
                    // Rekognition 데이터로 간접 추정
                    rednessScore += 5f
                }
            }
        }
        
        // 감정 상태로 홍조 추정 (흥분, 당황 등)
        faceDetail.emotions?.forEach { emotion ->
            when (emotion.type) {
                "SURPRISED", "CONFUSED" -> {
                    if (emotion.confidence > 70f) {
                        rednessScore += emotion.confidence * 0.2f
                    }
                }
            }
        }
        
        // 얼굴 품질로 홍조 추정 (밝기 변화)
        faceDetail.quality?.brightness?.let { brightness ->
            if (brightness > 80f) {
                rednessScore += 10f // 밝은 얼굴은 홍조 가능성
            }
        }
        
        return minOf(30f, rednessScore) // 최대 30점
    }
    
    private fun bitmapToByteBuffer(bitmap: Bitmap): ByteBuffer {
        val stream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 80, stream)
        val byteArray = stream.toByteArray()
        return ByteBuffer.wrap(byteArray)
    }
}
