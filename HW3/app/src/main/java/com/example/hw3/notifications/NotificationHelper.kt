package com.example.hw3.notifications

import android.Manifest
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.example.hw3.MainActivity
import com.example.hw3.R

class NotificationHelper(private val context: Context) {

    private val manager: NotificationManager =
        context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    init {
        createChannels()
    }

    /** До Android 13 разрешение на уведомления не требуется. */
    fun hasPermission(): Boolean =
        Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
                ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.POST_NOTIFICATIONS,
                ) == PackageManager.PERMISSION_GRANTED

    fun getActiveIds(): List<Int> =
        manager.activeNotifications.map { it.id }.sorted()

    fun show(
        title: String,
        text: String,
        importance: NotificationImportance,
        isCancellable: Boolean,
    ) {
        val id = (manager.activeNotifications.maxOfOrNull { it.id } ?: 0) + 1
        post(id, title, text, importance, isCancellable)
    }

    /**
     * Меняет заголовок и текст активного уведомления, сохраняя его importance и cancellable.
     * Возвращает false, если уведомления с таким id нет.
     */
    fun update(id: Int, title: String, text: String): Boolean {
        val notification = manager.activeNotifications
            .firstOrNull { it.id == id }
            ?.notification
            ?: return false
        val isOngoing = (notification.flags and Notification.FLAG_ONGOING_EVENT) != 0
        post(id, title, text, NotificationImportance.fromNotification(notification), !isOngoing)
        return true
    }

    fun cancelAll() {
        manager.cancelAll()
    }

    private fun post(
        id: Int,
        title: String,
        text: String,
        importance: NotificationImportance,
        isCancellable: Boolean,
    ) {
        val notification = NotificationCompat.Builder(context, importance.channelId)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(title)
            .setContentText(text)
            .setStyle(NotificationCompat.BigTextStyle().bigText(text))
            .setPriority(importance.legacyPriority)
            .setAutoCancel(isCancellable)
            .setOngoing(!isCancellable)
            .setContentIntent(createContentIntent(id, title, text))
            .build()
        manager.notify(id, notification)
    }

    private fun createContentIntent(id: Int, title: String, text: String): PendingIntent {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra(NotificationConstants.EXTRA_TITLE, title)
            putExtra(NotificationConstants.EXTRA_TEXT, text)
        }
        // requestCode = id, чтобы у каждого уведомления был свой PendingIntent со своими extras
        return PendingIntent.getActivity(
            context,
            id,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
    }

    private fun createChannels() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        NotificationImportance.values().forEach { importance ->
            manager.createNotificationChannel(
                NotificationChannel(
                    importance.channelId,
                    context.getString(importance.labelRes),
                    importance.channelImportance,
                ),
            )
        }
    }
}