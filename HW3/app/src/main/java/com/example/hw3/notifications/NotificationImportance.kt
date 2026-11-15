package com.example.hw3.notifications

import android.app.Notification
import android.app.NotificationManager
import androidx.annotation.StringRes
import androidx.core.app.NotificationCompat
import com.example.hw3.R

/**
 * Importance уведомления.
 * Начиная с Android 8 importance задаётся через канал, поэтому у каждого значения свой канал.
 * Для Android ниже 8 используется legacyPriority.
 */
enum class NotificationImportance(
    @StringRes val labelRes: Int,
    val channelId: String,
    val channelImportance: Int,
    val legacyPriority: Int,
) {
    MIN(
        R.string.importance_min,
        NotificationConstants.CHANNEL_ID_MIN,
        NotificationManager.IMPORTANCE_MIN,
        NotificationCompat.PRIORITY_MIN,
    ),
    DEFAULT(
        R.string.importance_default,
        NotificationConstants.CHANNEL_ID_DEFAULT,
        NotificationManager.IMPORTANCE_DEFAULT,
        NotificationCompat.PRIORITY_DEFAULT,
    ),
    HIGH(
        R.string.importance_high,
        NotificationConstants.CHANNEL_ID_HIGH,
        NotificationManager.IMPORTANCE_HIGH,
        NotificationCompat.PRIORITY_HIGH,
    );

    companion object {
        /** Определяет importance уже показанного уведомления (нужно при редактировании). */
        @Suppress("DEPRECATION")
        fun fromNotification(notification: Notification): NotificationImportance {
            val channelId = NotificationCompat.getChannelId(notification)
            return values().firstOrNull { it.channelId == channelId }
                ?: values().firstOrNull { it.legacyPriority == notification.priority }
                ?: DEFAULT
        }
    }
}