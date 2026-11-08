package com.example.sensor

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import kotlin.math.sqrt

class DropDetector(
    private val context: Context,
    private val onGForceUpdated: (Float) -> Unit,
    private val onDropDetected: () -> Unit
) : SensorEventListener {

    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as? SensorManager
    private val accelerometer: Sensor? = sensorManager?.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)

    private var isListening = false
    private var freeFallDetectedTime: Long = 0L
    private val freeFallThreshold = 3.5f // m/s^2 (~0.35G)
    private val impactThreshold = 24.0f // m/s^2 (~2.4G)
    private val impactWindowMs = 800L // impact must happen within 800ms of free fall

    fun start() {
        if (isListening || accelerometer == null) return
        sensorManager?.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_UI)
        isListening = true
    }

    fun stop() {
        if (!isListening) return
        sensorManager?.unregisterListener(this)
        isListening = false
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event == null || event.sensor.type != Sensor.TYPE_ACCELEROMETER) return

        val x = event.values[0]
        val y = event.values[1]
        val z = event.values[2]
        val magnitude = sqrt((x * x + y * y + z * z).toDouble()).toFloat()
        val gForce = magnitude / 9.80665f

        onGForceUpdated(gForce)

        val now = System.currentTimeMillis()

        // Step 1: Detect free fall
        if (magnitude < freeFallThreshold) {
            freeFallDetectedTime = now
        }

        // Step 2: Detect subsequent impact within window
        if (magnitude > impactThreshold && (now - freeFallDetectedTime) < impactWindowMs) {
            freeFallDetectedTime = 0L
            onDropDetected()
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}

    fun simulateDrop() {
        onGForceUpdated(0.15f)
        onDropDetected()
    }
}
