package com.hackathon.alcolook

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.IOException

data class TestResult(
    val imageName: String,
    val expectedLevel: Int, // 0-100
    val predictedLevel: Int,
    val accuracy: Float
)

class TestDatasetEvaluator(private val context: Context) {
    
    companion object {
        private const val TAG = "TestEvaluator"
    }
    
    private val drunkDetectionService = PhotoDrunkDetectionService(context)
    
    // 테스트 데이터셋 정의 (assets/test_images/ 폴더에 이미지 저장)
    private val testDataset = listOf(
        // 정상 상태 (0-20%)
        "sober_1.jpg" to 10,
        "sober_2.jpg" to 5,
        "sober_3.jpg" to 15,
        
        // 약간 취함 (21-50%)
        "slightly_drunk_1.jpg" to 30,
        "slightly_drunk_2.jpg" to 40,
        "slightly_drunk_3.jpg" to 35,
        
        // 많이 취함 (51-80%)
        "drunk_1.jpg" to 60,
        "drunk_2.jpg" to 70,
        "drunk_3.jpg" to 65,
        
        // 매우 취함 (81-100%)
        "very_drunk_1.jpg" to 90,
        "very_drunk_2.jpg" to 85,
        "very_drunk_3.jpg" to 95
    )
    
    suspend fun runEvaluation(): List<TestResult> = withContext(Dispatchers.IO) {
        val results = mutableListOf<TestResult>()
        
        Log.d(TAG, "=== 테스트 데이터셋 평가 시작 ===")
        
        for ((imageName, expectedLevel) in testDataset) {
            try {
                val bitmap = loadImageFromAssets(imageName)
                if (bitmap != null) {
                    val analysisResult = drunkDetectionService.detectDrunkLevel(bitmap)
                    val predictedLevel = analysisResult.drunkPercentage.toInt()
                    val accuracy = calculateAccuracy(expectedLevel, predictedLevel)
                    
                    val result = TestResult(imageName, expectedLevel, predictedLevel, accuracy)
                    results.add(result)
                    
                    Log.d(TAG, "${imageName}: 예상=${expectedLevel}%, 예측=${predictedLevel}%, 정확도=${accuracy}%")
                } else {
                    Log.e(TAG, "이미지 로드 실패: $imageName")
                }
            } catch (e: Exception) {
                Log.e(TAG, "평가 실패 - $imageName: ${e.message}")
            }
        }
        
        // 전체 통계
        val avgAccuracy = results.map { it.accuracy }.average()
        Log.d(TAG, "=== 평가 완료 ===")
        Log.d(TAG, "전체 평균 정확도: ${avgAccuracy}%")
        Log.d(TAG, "테스트 이미지 수: ${results.size}/${testDataset.size}")
        
        results
    }
    
    private fun loadImageFromAssets(fileName: String): Bitmap? {
        return try {
            val inputStream = context.assets.open("test_images/$fileName")
            BitmapFactory.decodeStream(inputStream)
        } catch (e: IOException) {
            Log.w(TAG, "테스트 이미지 없음: $fileName (실제 이미지 추가 필요)")
            null
        }
    }
    
    private fun calculateAccuracy(expected: Int, predicted: Int): Float {
        val error = kotlin.math.abs(expected - predicted)
        return kotlin.math.max(0f, 100f - error)
    }
    
    // 실시간 정확도 개선을 위한 임계값 조정
    fun suggestThresholdAdjustments(results: List<TestResult>) {
        Log.d(TAG, "=== 임계값 조정 제안 ===")
        
        val falsePositives = results.filter { it.expectedLevel < 30 && it.predictedLevel > 50 }
        val falseNegatives = results.filter { it.expectedLevel > 70 && it.predictedLevel < 50 }
        
        Log.d(TAG, "거짓 양성 (정상인데 취함): ${falsePositives.size}개")
        Log.d(TAG, "거짓 음성 (취했는데 정상): ${falseNegatives.size}개")
        
        if (falsePositives.size > falseNegatives.size) {
            Log.d(TAG, "제안: 임계값을 높여서 민감도 낮추기")
        } else if (falseNegatives.size > falsePositives.size) {
            Log.d(TAG, "제안: 임계값을 낮춰서 민감도 높이기")
        }
    }
}
