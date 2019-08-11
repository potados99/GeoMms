/*
 * Copyright (C) 2017 Moez Bhatti <moez.bhatti@gmail.com>
 *
 * This file is part of QKSMS.
 *
 * QKSMS is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * QKSMS is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with QKSMS.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.potados.geomms.common.manager

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.ContentUris
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.provider.ContactsContract
import android.telephony.PhoneNumberUtils
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.app.Person
import androidx.core.app.RemoteInput
import androidx.core.app.TaskStackBuilder
import androidx.core.graphics.drawable.IconCompat
import com.potados.geomms.feature.compose.ComposeActivity
import com.potados.geomms.manager.PermissionManager
import com.potados.geomms.preference.Preferences
import com.potados.geomms.repository.ConversationRepository
import com.potados.geomms.repository.MessageRepository
import timber.log.Timber

class NotificationManagerImplTest() : com.potados.geomms.manager.NotificationManager {

    override fun buildNotificationChannelId(threadId: Long): String {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun createNotificationChannel(threadId: Long) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }


    override fun getNotificationForBackup(): NotificationCompat.Builder {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun notifyFailed(threadId: Long) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun update(threadId: Long) {
        Timber.i("update thread $threadId")
    }
}