package com.hackathon.alcolook

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlin.math.abs
import kotlin.math.sqrt

class GyroscopeManager(context: Context) : SensorEventListener {
    
    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    private val gyroscope = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE)
    
    private val _walkingData = MutableStateFlow(WalkingTestData())
    val walkingData: StateFlow<WalkingTestData> = _walkingData
    
    private var isRecording = false
    private var startTime = 0L
    private val gyroReadings = mutableListOf<GyroReading>()
    
    fun isGyroscopeAvailable(): Boolean {
        return gyroscope != null
    }
    
    fun startTest() {
        if (gyroscope != null) {
            isRecording = true
            startTime = System.currentTimeMillis()
            gyroReadings.clear()
            
            // 공식 문서 권장: SENSOR_DELAY_GAME (20ms)
            val success = sensorManager.registerListener(
                this, 
                gyroscope, 
                SensorManager.SENSOR_DELAY_GAME
            )
            
            if (success) {
                _walkingData.value = WalkingTestData(
                    isRecording = true,
                    phase = TestPhase.FIRST_10_STEPS
                )
            }
        }
    }
    
    fun stopTest() {
        isRecording = false
        sensorManager.unregisterListener(this)
        
        val score = calculateScore()
        _walkingData.value = _walkingData.value.copy(
            isRecording = false,
            score = score,
            phase = TestPhase.COMPLETED
        )
    }
    
    fun nextPhase() {
        val currentPhase = _walkingData.value.phase
        val nextPhase = when (currentPhase) {
            TestPhase.FIRST_10_STEPS -> TestPhase.TURN
            TestPhase.TURN -> TestPhase.SECOND_10_STEPS
            TestPhase.SECOND_10_STEPS -> TestPhase.COMPLETED
            else -> TestPhase.COMPLETED
        }
        
        _walkingData.value = _walkingData.value.copy(phase = nextPhase)
    }
    
    override fun onSensorChanged(event: SensorEvent?) {
        if (!isRecording || event?.sensor?.type != Sensor.TYPE_GYROSCOPE) return
        
        // 공식 문서: 자이로스코프 값은 rad/s 단위
        val x = event.values[0] // X축 회전 속도 (pitch)
        val y = event.values[1] // Y축 회전 속도 (roll) 
        val z = event.values[2] // Z축 회전 속도 (yaw)
        
        val timestamp = System.currentTimeMillis() - startTime
        gyroReadings.add(GyroReading(x, y, z, timestamp))
        
        // 실시간 불안정성 계산 (수평 변화 중심)
        val instability = calculateInstability(x, y, z)
        _walkingData.value = _walkingData.value.copy(
            currentInstability = instability,
            duration = timestamp
        )
    }
    
    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        // 정확도 변화 처리 (필요시)
    }
    
    private fun calculateInstability(x: Float, y: Float, z: Float): Float {
        // 수평 불안정성: X(pitch)와 Y(roll) 축의 회전 속도
        // Z축(yaw)은 의도적 회전이므로 가중치 낮게
        return sqrt(x * x + y * y + (z * z * 0.3f))
    }
    
    private fun calculateScore(): Int {
        if (gyroReadings.isEmpty()) return 0
        
        // 각 단계별 불안정성 계산
        val totalDuration = gyroReadings.last().timestamp
        val firstPhaseEnd = totalDuration * 0.4f
        val turnPhaseEnd = totalDuration * 0.6f
        
        val firstPhaseReadings = gyroReadings.filter { it.timestamp <= firstPhaseEnd }
        val turnPhaseReadings = gyroReadings.filter { 
            it.timestamp > firstPhaseEnd && it.timestamp <= turnPhaseEnd 
        }
        val secondPhaseReadings = gyroReadings.filter { it.timestamp > turnPhaseEnd }
        
        val firstPhaseScore = calculatePhaseScore(firstPhaseReadings)
        val turnPhaseScore = calculatePhaseScore(turnPhaseReadings) * 0.5f // 턴은 가중치 낮게
        val secondPhaseScore = calculatePhaseScore(secondPhaseReadings)
        
        val totalScore = (firstPhaseScore + turnPhaseScore + secondPhaseScore) / 2.5f
        
        // 100점 만점으로 변환 (불안정성이 낮을수록 높은 점수)
        // 공식 문서 기준: 일반적으로 0.1 rad/s 이하가 안정적
        val normalizedScore = (1.0f - (totalScore / 0.5f).coerceAtMost(1.0f)) * 100
        return normalizedScore.toInt().coerceIn(0, 100)
    }
    
    private fun calculatePhaseScore(readings: List<GyroReading>): Float {
        if (readings.isEmpty()) return 0f
        
        val instabilities = readings.map { 
            calculateInstability(it.x, it.y, it.z) 
        }
        
        return instabilities.average().toFloat()
    }
    
    fun cleanup() {
        if (isRecording) {
            sensorManager.unregisterListener(this)
            isRecording = false
        }
    }
}

data class WalkingTestData(
    val isRecording: Boolean = false,
    val phase: TestPhase = TestPhase.READY,
    val currentInstability: Float = 0f,
    val duration: Long = 0L,
    val score: Int = 0
)

data class GyroReading(
    val x: Float, // rad/s
    val y: Float, // rad/s
    val z: Float, // rad/s
    val timestamp: Long
)

enum class TestPhase {
    READY, FIRST_10_STEPS, TURN, SECOND_10_STEPS, COMPLETED
}
