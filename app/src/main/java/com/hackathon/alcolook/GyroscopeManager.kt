package com.hackathon.alcolook

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.util.Log
import kotlin.math.abs
import kotlin.math.sqrt

class GyroscopeManager(private val context: Context) : SensorEventListener {
    
    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    private val gyroscopeSensor = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE)
    
    private var isRecording = false
    private val gyroData = mutableListOf<GyroReading>()
    private var onResultCallback: ((WalkingTestResult) -> Unit)? = null
    
    // 보행 불안정성 감지 임계값
    private val instabilityThreshold = 2.0f // 각속도 임계값 (rad/s)
    private val minRecordingTime = 5000L // 최소 측정 시간 (5초)
    private var recordingStartTime = 0L
    
    fun startRecording(callback: (WalkingTestResult) -> Unit) {
        if (gyroscopeSensor == null) {
            Log.e("GyroscopeManager", "자이로스코프 센서를 찾을 수 없습니다")
            callback(WalkingTestResult.ERROR)
            return
        }
        
        isRecording = true
        gyroData.clear()
        onResultCallback = callback
        recordingStartTime = System.currentTimeMillis()
        
        sensorManager.registerListener(this, gyroscopeSensor, SensorManager.SENSOR_DELAY_GAME)
        Log.d("GyroscopeManager", "자이로스코프 측정 시작")
    }
    
    fun stopRecording() {
        if (!isRecording) return
        
        isRecording = false
        sensorManager.unregisterListener(this)
        
        val recordingDuration = System.currentTimeMillis() - recordingStartTime
        if (recordingDuration < minRecordingTime) {
            Log.w("GyroscopeManager", "측정 시간이 너무 짧습니다: ${recordingDuration}ms")
            onResultCallback?.invoke(WalkingTestResult.INSUFFICIENT_DATA)
            return
        }
        
        val result = analyzeWalkingStability()
        Log.d("GyroscopeManager", "보행 분석 결과: $result")
        onResultCallback?.invoke(result)
    }
    
    override fun onSensorChanged(event: SensorEvent?) {
        if (!isRecording || event?.sensor?.type != Sensor.TYPE_GYROSCOPE) return
        
        val x = event.values[0]
        val y = event.values[1] 
        val z = event.values[2]
        
        val magnitude = sqrt(x * x + y * y + z * z)
        val timestamp = System.currentTimeMillis()
        
        gyroData.add(GyroReading(x, y, z, magnitude, timestamp))
        
        Log.d("GyroscopeManager", "자이로 데이터: x=$x, y=$y, z=$z, magnitude=$magnitude")
    }
    
    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        // 정확도 변경 시 처리 (필요시)
    }
    
    private fun analyzeWalkingStability(): WalkingTestResult {
        if (gyroData.isEmpty()) {
            return WalkingTestResult.INSUFFICIENT_DATA
        }
        
        // 불안정한 움직임 감지
        val instableReadings = gyroData.count { it.magnitude > instabilityThreshold }
        val instabilityRatio = instableReadings.toFloat() / gyroData.size
        
        // 급격한 방향 변화 감지
        val suddenChanges = detectSuddenChanges()
        
        Log.d("GyroscopeManager", "총 데이터: ${gyroData.size}, 불안정 비율: $instabilityRatio, 급격한 변화: $suddenChanges")
        
        return when {
            instabilityRatio > 0.3f || suddenChanges > 10 -> WalkingTestResult.UNSTABLE
            instabilityRatio > 0.15f || suddenChanges > 5 -> WalkingTestResult.SLIGHTLY_UNSTABLE
            else -> WalkingTestResult.STABLE
        }
    }
    
    private fun detectSuddenChanges(): Int {
        if (gyroData.size < 2) return 0
        
        var suddenChanges = 0
        val changeThreshold = 1.5f
        
        for (i in 1 until gyroData.size) {
            val prev = gyroData[i - 1]
            val curr = gyroData[i]
            
            val deltaX = abs(curr.x - prev.x)
            val deltaY = abs(curr.y - prev.y)
            val deltaZ = abs(curr.z - prev.z)
            
            if (deltaX > changeThreshold || deltaY > changeThreshold || deltaZ > changeThreshold) {
                suddenChanges++
            }
        }
        
        return suddenChanges
    }
}

data class GyroReading(
    val x: Float,
    val y: Float,
    val z: Float,
    val magnitude: Float,
    val timestamp: Long
)

enum class WalkingTestResult {
    STABLE,           // 안정적인 보행
    SLIGHTLY_UNSTABLE, // 약간 불안정
    UNSTABLE,         // 불안정한 보행
    INSUFFICIENT_DATA, // 데이터 부족
    ERROR             // 센서 오류
}
