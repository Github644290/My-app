package com.example.sensor

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter

class PowerButtonDetector(
    private val context: Context,
    private val onCountUpdated: (Int) -> Unit,
    private val onIntentionalPowerPress: () -> Unit
) {
    private var isListening = false
    private var pressCount = 0
    private var lastPressTimestamp: Long = 0L
    private val multiPressWindowMs = 3500L // 3.5 seconds window for deliberate rapid presses
    private val requiredPresses = 3 // Standard intentional trigger

    private val screenReceiver = object : BroadcastReceiver() {
        override fun onReceive(c: Context?, intent: Intent?) {
            val action = intent?.action
            if (action == Intent.ACTION_SCREEN_OFF || action == Intent.ACTION_SCREEN_ON) {
                handlePowerPress()
            }
        }
    }

    fun start() {
        if (isListening) return
        val filter = IntentFilter().apply {
            addAction(Intent.ACTION_SCREEN_OFF)
            addAction(Intent.ACTION_SCREEN_ON)
        }
        context.registerReceiver(screenReceiver, filter)
        isListening = true
    }

    fun stop() {
        if (!isListening) return
        try {
            context.unregisterReceiver(screenReceiver)
        } catch (ignored: Exception) {}
        isListening = false
    }

    private fun handlePowerPress() {
        val now = System.currentTimeMillis()
        if (now - lastPressTimestamp > multiPressWindowMs) {
            pressCount = 1
        } else {
            pressCount++
        }
        lastPressTimestamp = now
        onCountUpdated(pressCount)

        if (pressCount >= requiredPresses) {
            pressCount = 0
            onCountUpdated(0)
            onIntentionalPowerPress()
        }
    }

    fun simulatePowerPress() {
        handlePowerPress()
        handlePowerPress()
        handlePowerPress()
    }
}
