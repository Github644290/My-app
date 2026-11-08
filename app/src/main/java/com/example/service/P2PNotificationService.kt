package com.example.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import androidx.core.app.ServiceCompat
import com.example.MainActivity
import com.example.R

class P2PNotificationService : Service() {

    companion object {
        const val CHANNEL_ID = "p2p_channel_status"
        const val NOTIFICATION_ID = 1001

        const val ACTION_START = "ACTION_START_P2P_NOTIFICATION"
        const val ACTION_STOP = "ACTION_STOP_P2P_NOTIFICATION"

        fun start(context: Context) {
            val intent = Intent(context, P2PNotificationService::class.java).apply {
                action = ACTION_START
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }

        fun stop(context: Context) {
            val intent = Intent(context, P2PNotificationService::class.java).apply {
                action = ACTION_STOP
            }
            context.startService(intent)
        }
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_STOP -> {
                stopForeground(STOP_FOREGROUND_REMOVE)
                stopSelf()
            }
            ACTION_START, null -> {
                val notification = buildPersistentNotification()
                val foregroundServiceType = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    ServiceInfo.FOREGROUND_SERVICE_TYPE_CONNECTED_DEVICE
                } else {
                    0
                }
                ServiceCompat.startForeground(this, NOTIFICATION_ID, notification, foregroundServiceType)
            }
        }
        return START_STICKY
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = getString(R.string.notification_channel_name)
            val descriptionText = getString(R.string.notification_channel_desc)
            val importance = NotificationManager.IMPORTANCE_LOW // Low so it remains persistent and quiet like YouTube
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
                setShowBadge(true)
            }
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun buildPersistentNotification(): Notification {
        val launchIntent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            launchIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Pairing Successful")
            .setContentText("P2P connection is active and secured • Both devices linked")
            .setSubText("P2P Active")
            .setSmallIcon(R.mipmap.ic_launcher)
            .setOngoing(true) // Persistent notification like YouTube background player
            .setAutoCancel(false)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setContentIntent(pendingIntent)
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText("Pairing Successful\nServerless P2P tunnel active. Both devices authenticated with matching credentials. Ready for encrypted voice, video, and data transmission.")
            )
            .build()
    }
}
