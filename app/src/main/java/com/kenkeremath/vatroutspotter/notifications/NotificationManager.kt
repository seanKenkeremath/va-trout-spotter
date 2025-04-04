package com.kenkeremath.vatroutspotter.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NotificationManager @Inject constructor(
    @ApplicationContext val context: Context
) {
    private val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    fun createNotificationChannel() {
        val channel = NotificationChannel(
            CHANNEL_ID,
            CHANNEL_NAME,
            NotificationManager.IMPORTANCE_DEFAULT
        ).apply {
            description = CHANNEL_DESCRIPTION
        }
        notificationManager.createNotificationChannel(channel)
    }

    fun notify(id: Int, notification: android.app.Notification) {
        notificationManager.notify(id, notification)
    }

    companion object {
        const val CHANNEL_ID = "stocking_notifications"
        private const val CHANNEL_NAME = "Stocking Notifications"
        private const val CHANNEL_DESCRIPTION = "Notifications for new trout stockings"
    }
} 