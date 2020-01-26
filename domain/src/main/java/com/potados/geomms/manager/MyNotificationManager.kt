/*
 * Copyright (C) 2019-2020 Song Byeong Jun <potados99@gmail.com>
 *
 * This file is part of GeoMms.
 *
 * GeoMms is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * GeoMms is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with GeoMms.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.potados.geomms.manager

import androidx.core.app.NotificationCompat

abstract class MyNotificationManager : Manager() {
    abstract fun updateThread(threadId: Long)

    abstract fun updateConnection(connectionId: Long, type: Int)

    abstract fun notifyFailed(msgId: Long)

    abstract fun createNotificationChannel(threadId: Long)

    abstract fun createNotificationChannelForConnection(connectionId: Long)

    abstract fun buildNotificationChannelId(threadId: Long): String

    abstract fun getNotificationForBackup(): NotificationCompat.Builder

    companion object {
        // To use when we have to let "updateConnection" know why we called it.
        const val CONNECTION_INVITATION = 1
        const val CONNECTION_ESTABLISHED = 2
    }
}