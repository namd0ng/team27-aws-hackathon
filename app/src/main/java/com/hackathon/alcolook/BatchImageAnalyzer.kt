package com.hackathon.alcolook

import android.content.Context
import android.graphics.BitmapFactory
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

data class ImageAnalysisResult(
    val fileName: String,
    val drunkPercentage: Int,
    val hasError: Boolean = false
)

class BatchImageAnalyzer(private val context: Context) {
    
    companion object {
        private const val TAG = "BatchAnalyzer"
    }
    
    private val drunkDetectionService = PhotoDrunkDetectionService(context)
    
    suspend fun analyzeAllImages(): List<ImageAnalysisResult> = withContext(Dispatchers.IO) {
        val results = mutableListOf<ImageAnalysisResult>()
        val imageFolder = File("/mnt/c/Users/admin/Downloads/team27-aws-hackathon/drunk-images")
        
        if (!imageFolder.exists()) {
            Log.e(TAG, "drunk-images 폴더를 찾을 수 없습니다")
            return@withContext results
        }
        
        val allFiles = imageFolder.listFiles() ?: arrayOf()
        val imageFiles = allFiles.filter { file ->
            val extension = file.extension.lowercase()
            extension == "jpg" || extension == "jpeg" || extension == "png"
        }.sortedBy { it.name }
        
        Log.d(TAG, "=== 배치 이미지 분석 시작 ===")
        Log.d(TAG, "총 ${imageFiles.size}개 이미지 발견")
        
        var processedCount = 0
        var totalDrunkLevel = 0
        var validResults = 0
        
        for (imageFile in imageFiles) {
            try {
                processedCount++
                Log.d(TAG, "[$processedCount/${imageFiles.size}] ${imageFile.name} 분석 중...")
                
                val bitmap = BitmapFactory.decodeFile(imageFile.absolutePath)
                if (bitmap != null) {
                    val result = drunkDetectionService.detectDrunkLevel(bitmap)
                    val drunkLevel = result.drunkPercentage.toInt()
                    results.add(ImageAnalysisResult(imageFile.name, drunkLevel))
                    
                    totalDrunkLevel += drunkLevel
                    validResults++
                    
                    Log.d(TAG, "${imageFile.name}: ${drunkLevel}%")
                } else {
                    Log.e(TAG, "${imageFile.name}: 이미지 로드 실패")
                    results.add(ImageAnalysisResult(imageFile.name, 0, hasError = true))
                }
                
                if (processedCount % 10 == 0) {
                    val currentAvg = if (validResults > 0) totalDrunkLevel / validResults else 0
                    Log.d(TAG, "진행률: $processedCount/${imageFiles.size} (현재 평균: ${currentAvg}%)")
                }
                
            } catch (e: Exception) {
                Log.e(TAG, "${imageFile.name} 분석 실패: ${e.message}")
                results.add(ImageAnalysisResult(imageFile.name, 0, hasError = true))
            }
        }
        
        val avgDrunkLevel = if (validResults > 0) totalDrunkLevel / validResults else 0
        val errorCount = results.count { it.hasError }
        
        Log.d(TAG, "=== 배치 분석 완료 ===")
        Log.d(TAG, "총 이미지: ${imageFiles.size}개")
        Log.d(TAG, "성공: ${validResults}개")
        Log.d(TAG, "실패: ${errorCount}개")
        Log.d(TAG, "평균 음주도: ${avgDrunkLevel}%")
        
        analyzeDistribution(results.filter { !it.hasError })
        
        results
    }
    
    private fun analyzeDistribution(validResults: List<ImageAnalysisResult>) {
        if (validResults.isEmpty()) return
        
        val levels = validResults.map { it.drunkPercentage }
        val sober = levels.count { it < 20 }
        val light = levels.count { it in 20..40 }
        val moderate = levels.count { it in 41..70 }
        val heavy = levels.count { it > 70 }
        
        Log.d(TAG, "=== 음주도 분포 ===")
        Log.d(TAG, "정상 (0-19%): ${sober}개")
        Log.d(TAG, "약간 취함 (20-40%): ${light}개")
        Log.d(TAG, "취함 (41-70%): ${moderate}개")
        Log.d(TAG, "매우 취함 (71-100%): ${heavy}개")
        
        val min = levels.minOrNull() ?: 0
        val max = levels.maxOrNull() ?: 0
        val sorted = levels.sorted()
        val median = if (sorted.size % 2 == 0) {
            (sorted[sorted.size/2 - 1] + sorted[sorted.size/2]) / 2
        } else {
            sorted[sorted.size/2]
        }
        
        Log.d(TAG, "최소값: ${min}%, 최대값: ${max}%, 중간값: ${median}%")
    }
}
