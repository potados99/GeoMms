package com.potados.geomms.manager

import androidx.core.app.NotificationCompat

abstract class NotificationManager : Manager() {
    abstract fun update(threadId: Long)

    abstract fun notifyFailed(msgId: Long)

    abstract fun createNotificationChannel(threadId: Long)

    abstract fun buildNotificationChannelId(threadId: Long): String

    abstract fun getNotificationForBackup(): NotificationCompat.Builder
}