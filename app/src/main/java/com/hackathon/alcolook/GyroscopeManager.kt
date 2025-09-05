package com.hackathon.alcolook

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager

enum class WalkingTestResult {
    STABLE,
    SLIGHTLY_UNSTABLE,
    UNSTABLE,
    INSUFFICIENT_DATA,
    ERROR
}

class GyroscopeManager(private val context: Context) : SensorEventListener {
    
    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    private val gyroscopeSensor = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE)
    
    private var isRecording = false
    private var onResultCallback: ((WalkingTestResult) -> Unit)? = null
    private var recordingStartTime = 0L
    
    fun startRecording(callback: (WalkingTestResult) -> Unit) {
        if (gyroscopeSensor == null) {
            callback(WalkingTestResult.ERROR)
            return
        }
        
        isRecording = true
        onResultCallback = callback
        recordingStartTime = System.currentTimeMillis()
        
        sensorManager.registerListener(this, gyroscopeSensor, SensorManager.SENSOR_DELAY_GAME)
    }
    
    fun stopRecording() {
        if (!isRecording) return
        
        isRecording = false
        sensorManager.unregisterListener(this)
        
        // 간단한 랜덤 결과 생성
        val result = when ((Math.random() * 3).toInt()) {
            0 -> WalkingTestResult.STABLE
            1 -> WalkingTestResult.SLIGHTLY_UNSTABLE
            else -> WalkingTestResult.UNSTABLE
        }
        
        onResultCallback?.invoke(result)
    }
    
    override fun onSensorChanged(event: SensorEvent?) {
        // 센서 데이터 처리 (간단 버전)
    }
    
    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        // 정확도 변경 처리
    }
}
