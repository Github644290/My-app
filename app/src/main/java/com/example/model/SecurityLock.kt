package com.example.model

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class DeviceSecurityLock(
    val deviceImeiOrId: String,
    val lockedUsername: String?,
    val lockStartTimeMillis: Long,
    val isLocked: Boolean
) {
    companion object {
        const val LOCK_DURATION_MILLIS: Long = 15L * 24 * 60 * 60 * 1000L // Strict 15 days

        fun calculateRemainingMillis(lockStartTime: Long): Long {
            if (lockStartTime <= 0L) return 0L
            val now = System.currentTimeMillis()
            val expiry = lockStartTime + LOCK_DURATION_MILLIS
            return if (expiry > now) expiry - now else 0L
        }

        fun formatRemaining(remainingMillis: Long): String {
            if (remainingMillis <= 0L) return "0 days, 0 hours"
            val totalSeconds = remainingMillis / 1000
            val days = totalSeconds / (24 * 3600)
            val hours = (totalSeconds % (24 * 3600)) / 3600
            val minutes = (totalSeconds % 3600) / 60
            return "$days days, $hours hrs, $minutes mins"
        }

        fun formatExpiryDate(lockStartTime: Long): String {
            if (lockStartTime <= 0L) return "N/A"
            val expiry = Date(lockStartTime + LOCK_DURATION_MILLIS)
            val sdf = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault())
            return sdf.format(expiry)
        }
    }

    val remainingMillis: Long get() = calculateRemainingMillis(lockStartTimeMillis)
    val remainingFormatted: String get() = formatRemaining(remainingMillis)
    val expiryFormatted: String get() = formatExpiryDate(lockStartTimeMillis)
    val isPeriodActive: Boolean get() = lockedUsername != null && remainingMillis > 0L
}
