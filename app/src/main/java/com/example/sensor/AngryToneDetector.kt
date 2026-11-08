package com.example.sensor

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import androidx.core.content.ContextCompat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.math.log10
import kotlin.math.sqrt

class AngryToneDetector(
    private val context: Context,
    private val onDecibelUpdated: (Float) -> Unit,
    private val onAngryToneDetected: (Float) -> Unit
) {
    private var recordingJob: Job? = null
    private var isRunning = false
    private val angryThresholdDb = 74.0f // Threshold for aggressive / angry volume and stress

    fun start(scope: CoroutineScope) {
        if (isRunning) return
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            // Permission not yet granted, detector will stand by
            return
        }

        isRunning = true
        recordingJob = scope.launch(Dispatchers.IO) {
            val sampleRate = 44100
            val channelConfig = AudioFormat.CHANNEL_IN_MONO
            val audioFormat = AudioFormat.ENCODING_PCM_16BIT
            val bufferSize = AudioRecord.getMinBufferSize(sampleRate, channelConfig, audioFormat).coerceAtLeast(2048)

            var audioRecord: AudioRecord? = null
            try {
                audioRecord = AudioRecord(
                    MediaRecorder.AudioSource.MIC,
                    sampleRate,
                    channelConfig,
                    audioFormat,
                    bufferSize
                )

                if (audioRecord.state != AudioRecord.STATE_INITIALIZED) {
                    return@launch
                }

                audioRecord.startRecording()
                val buffer = ShortArray(bufferSize)

                var highStressFrameCount = 0

                while (isActive && isRunning) {
                    val read = audioRecord.read(buffer, 0, buffer.size)
                    if (read > 0) {
                        var sum = 0.0
                        for (i in 0 until read) {
                            val sample = buffer[i].toDouble()
                            sum += sample * sample
                        }
                        val rms = sqrt(sum / read)
                        val db = if (rms > 0.0) {
                            (20 * log10(rms)).toFloat().coerceIn(20f, 100f)
                        } else {
                            20f
                        }

                        onDecibelUpdated(db)

                        if (db >= angryThresholdDb) {
                            highStressFrameCount++
                            // Sustained loud/aggressive stress (e.g. shouting or angry spikes)
                            if (highStressFrameCount >= 3) {
                                highStressFrameCount = 0
                                onAngryToneDetected(db)
                            }
                        } else {
                            if (highStressFrameCount > 0) highStressFrameCount--
                        }
                    }
                    kotlinx.coroutines.delay(100)
                }
            } catch (e: Exception) {
                // Silently handle audio exception
            } finally {
                try {
                    audioRecord?.stop()
                    audioRecord?.release()
                } catch (ignored: Exception) {}
            }
        }
    }

    fun stop() {
        isRunning = false
        recordingJob?.cancel()
        recordingJob = null
    }

    fun simulateAngryTone() {
        onDecibelUpdated(86.5f)
        onAngryToneDetected(86.5f)
    }
}
