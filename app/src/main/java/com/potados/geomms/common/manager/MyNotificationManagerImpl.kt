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

package com.potados.geomms.common.manager

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.ContentUris
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.os.Build
import android.provider.ContactsContract
import android.telephony.PhoneNumberUtils
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationCompat.BADGE_ICON_SMALL
import androidx.core.app.NotificationManagerCompat
import androidx.core.app.Person
import androidx.core.app.TaskStackBuilder
import androidx.core.graphics.drawable.IconCompat
import com.bumptech.glide.Glide
import com.potados.geomms.R
import com.potados.geomms.common.extension.resolveThemeColor
import com.potados.geomms.extension.dpToPx
import com.potados.geomms.extension.isImage
import com.potados.geomms.extension.tryOrNull
import com.potados.geomms.feature.compose.ComposeActivity
import com.potados.geomms.feature.main.MainActivity
import com.potados.geomms.manager.MyNotificationManager
import com.potados.geomms.manager.PermissionManager
import com.potados.geomms.mapper.CursorToPartImpl
import com.potados.geomms.receiver.MarkReadReceiver
import com.potados.geomms.receiver.MarkSeenReceiver
import com.potados.geomms.repository.ConversationRepository
import com.potados.geomms.repository.MessageRepository
import com.potados.geomms.service.LocationSupportService
import org.koin.core.inject
import timber.log.Timber
import java.lang.RuntimeException

class MyNotificationManagerImpl(
    private val context: Context,
    private val conversationRepo: ConversationRepository,
    private val messageRepo: MessageRepository,
    private val permissionManager: PermissionManager
) : MyNotificationManager() {

    private val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    private val service: LocationSupportService by inject()

    init {
        @SuppressLint("NewApi")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Default"
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(DEFAULT_CHANNEL_ID, name, importance).apply {
                enableLights(true)
                lightColor = Color.WHITE
                enableVibration(true)
                vibrationPattern = VIBRATE_PATTERN
            }

            notificationManager.createNotificationChannel(channel)
        }
    }

    /**
     * Updates the notification for a particular conversation
     */
    override fun updateThread(threadId: Long) {
        val messages = messageRepo.getUnreadUnseenMessages(threadId) ?: return

        // If there are no messages to be displayed, make sure that the notification is dismissed
        if (messages.isEmpty()) {
            notificationManager.cancel(threadId.toInt())
            return
        }

        val conversation = conversationRepo.getConversation(threadId) ?: return

        val contentIntent = Intent(context, ComposeActivity::class.java).putExtra("threadId", threadId)
        val taskStackBuilder = TaskStackBuilder.create(context)
        taskStackBuilder.addParentStack(ComposeActivity::class.java)
        taskStackBuilder.addNextIntent(contentIntent)
        val contentPI = taskStackBuilder.getPendingIntent(threadId.toInt() + 10000, PendingIntent.FLAG_UPDATE_CURRENT)

        val seenIntent = Intent(context, MarkSeenReceiver::class.java).putExtra("threadId", threadId)
        val seenPI = PendingIntent.getBroadcast(context, threadId.toInt() + 20000, seenIntent, PendingIntent.FLAG_UPDATE_CURRENT)

        // We can't store a null preference, so map it to a null Uri if the pref string is empty
        val ringtone = Uri.parse("")

        val notification = NotificationCompat.Builder(context, getChannelIdForNotification(threadId))
            .setCategory(NotificationCompat.CATEGORY_MESSAGE)
            .setColor(context.resolveThemeColor(R.attr.tintPrimary))
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setSmallIcon(R.drawable.ic_notification)
            .setNumber(messages.size)
            .setAutoCancel(true)
            .setContentIntent(contentPI)
            .setDeleteIntent(seenPI)
            .setSound(ringtone)
            .setLights(Color.WHITE, 500, 2000)
            .setVibrate(VIBRATE_PATTERN)

        // Tell the notification if it's a group message
        val messagingStyle = NotificationCompat.MessagingStyle(Person.Builder().setName("Me").build())
        if (conversation.recipients.size >= 2) {
            messagingStyle.isGroupConversation = true
            messagingStyle.conversationTitle = conversation.getTitle()
        }

        // Add the messages to the notification
        messages.forEach { message ->
            val person = Person.Builder()

            if (!message.isMe()) {
                val recipient = conversation.recipients
                    .firstOrNull { PhoneNumberUtils.compare(it.address, message.address) }

                person.setName(recipient?.getDisplayName() ?: message.address)

                person.setIcon(
                    Glide.with(context)
                    .asBitmap()
                    .circleCrop()
                    .load(PhoneNumberUtils.stripSeparators(message.address))
                    .submit(64.dpToPx(context), 64.dpToPx(context))
                    .let { futureGet -> tryOrNull(false) { futureGet.get() } }
                    ?.let(IconCompat::createWithBitmap))

                recipient?.contact
                    ?.let { contact -> "${ContactsContract.Contacts.CONTENT_LOOKUP_URI}/${contact.lookupKey}" }
                    ?.let(person::setUri)
            }

            NotificationCompat.MessagingStyle.Message(message.getSummary(), message.date, person.build()).apply {
                message.parts.firstOrNull { it.isImage() }?.let { part ->
                    setData(part.type, ContentUris.withAppendedId(CursorToPartImpl.CONTENT_URI, part.id))
                }
                messagingStyle.addMessage(this)
            }
        }

        // Set the large icon
        val avatar = conversation.recipients.takeIf { it.size == 1 }
            ?.first()?.address
            ?.let { address ->
                Glide.with(context)
                    .asBitmap()
                    .circleCrop()
                    .load(PhoneNumberUtils.stripSeparators(address))
                    .submit(64.dpToPx(context), 64.dpToPx(context))
            }
            ?.let { futureGet -> tryOrNull(false) { futureGet.get() } }

        // Bind the notification contents based on the notification preview mode
        notification
            .setLargeIcon(avatar)
            .setStyle(messagingStyle)

        // Add all of the people from this conversation to the notification, so that the system can
        // appropriately bypass DND mode
        conversation.recipients
            .mapNotNull { recipient -> recipient.contact?.lookupKey }
            .forEach { uri -> notification.addPerson(uri) }

        val intent = Intent(context, MarkReadReceiver::class.java).putExtra("threadId", threadId)
        val pi = PendingIntent.getBroadcast(context, threadId.toInt() + 30000, intent, PendingIntent.FLAG_UPDATE_CURRENT)
        val noti = NotificationCompat.Action.Builder(R.drawable.ic_check_white_24dp, "Mark read", pi)
            .setSemanticAction(NotificationCompat.Action.SEMANTIC_ACTION_MARK_AS_READ).build()

        notification.addAction(noti)

        notificationManager.notify(threadId.toInt(), notification.build())
    }

    /**
     * Updates the notification for a particular connection.
     * Called when:
     * - I am Invited.
     * - My invitation is accepted.
     */
    override fun updateConnection(connectionId: Long, type: Int) {
        if (type != CONNECTION_INVITATION && type != CONNECTION_ESTABLISHED) return

        // We can't store a null preference, so map it to a null Uri if the pref string is empty
        val ringtone = Uri.parse("")

        val contentIntent = Intent(context, MainActivity::class.java)
        val taskStackBuilder = TaskStackBuilder.create(context)
        taskStackBuilder.addParentStack(MainActivity::class.java)
        taskStackBuilder.addNextIntent(contentIntent)
        val seenIntent = Intent(context, MarkSeenReceiver::class.java).putExtra("connectionId", connectionId)
        val seenPI = PendingIntent.getBroadcast(context, connectionId.toInt() + 20000, seenIntent, PendingIntent.FLAG_UPDATE_CURRENT)

        // Just show main activity when clicked.
        val contentPI = taskStackBuilder.getPendingIntent(connectionId.toInt() + 10000, PendingIntent.FLAG_UPDATE_CURRENT)

        val recipient = when (type) {
            CONNECTION_INVITATION -> service.getRequest(connectionId, inbound = true)?.recipient
            CONNECTION_ESTABLISHED -> service.getConnection(connectionId)?.recipient
            else -> throw RuntimeException("Wrong!!")
        }

        val recipientName = recipient?.getDisplayName() ?: "Unknown"

        val date = when (type) {
            CONNECTION_INVITATION -> service.getRequest(connectionId, inbound = true)?.date
            CONNECTION_ESTABLISHED -> service.getConnection(connectionId)?.date
            else -> throw RuntimeException("Wrong!!")
        } ?: throw RuntimeException("Wrong!!")

        val description = when (type) {
            CONNECTION_INVITATION -> context.getString(R.string.description_new_invitation, recipientName)
            CONNECTION_ESTABLISHED -> context.getString(R.string.description_invitation_accepted, recipientName)
            else -> throw RuntimeException("Wrong!!!")
        }

        // Create first.
        createNotificationChannelForConnection(connectionId)

        val notification = NotificationCompat.Builder(context, getChannelIdForNotification(connectionId))
            .setCategory(NotificationCompat.CATEGORY_MESSAGE)
            .setColor(context.resolveThemeColor(R.attr.tintPrimary))
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setSmallIcon(R.drawable.ic_notification)
            .setAutoCancel(true)
            .setSound(ringtone)
            .setLights(Color.WHITE, 500, 2000)
            .setVibrate(VIBRATE_PATTERN)
            .setContentIntent(contentPI)
            .setDeleteIntent(seenPI)
            .setContentTitle("GeoMMS")
            .setContentText(description)

        val messagingStyle = NotificationCompat.MessagingStyle(Person.Builder().setName("Me").build())

        val person = Person.Builder()

        person.setName(recipientName)

        person.setIcon(
            Glide.with(context)
                .asBitmap()
                .circleCrop()
                .load(PhoneNumberUtils.stripSeparators(recipient?.address))
                .submit(64.dpToPx(context), 64.dpToPx(context))
                .let { futureGet -> tryOrNull(false) { futureGet.get() } }
                ?.let(IconCompat::createWithBitmap))

        recipient?.contact
            ?.let { contact -> "${ContactsContract.Contacts.CONTENT_LOOKUP_URI}/${contact.lookupKey}" }
            ?.let(person::setUri)


        NotificationCompat.MessagingStyle.Message(description, date, person.build()).apply {
            messagingStyle.addMessage(this)
        }

        // Set the large icon
        val avatar = recipient?.address
            ?.let { address ->
                Glide.with(context)
                    .asBitmap()
                    .circleCrop()
                    .load(PhoneNumberUtils.stripSeparators(address))
                    .submit(64.dpToPx(context), 64.dpToPx(context))
            }
            ?.let { futureGet -> tryOrNull(false) { futureGet.get() } }

        // Bind the notification contents based on the notification preview mode
        notification
            .setLargeIcon(avatar)
            .setStyle(messagingStyle)

        // Add all of the people from this conversation to the notification, so that the system can
        // appropriately bypass DND mode
        notification.addPerson(recipient?.contact?.lookupKey)

        val intent = Intent(context, MarkReadReceiver::class.java).putExtra("threadId", connectionId)
        val pi = PendingIntent.getBroadcast(context, connectionId?.toInt() ?: 0 + 30000, intent, PendingIntent.FLAG_UPDATE_CURRENT)
        val noti = NotificationCompat.Action.Builder(R.drawable.ic_check_white_24dp, "Mark read", pi)
            .setSemanticAction(NotificationCompat.Action.SEMANTIC_ACTION_MARK_AS_READ).build()

        notification.addAction(noti)

        notificationManager.notify(connectionId.toInt(), notification.build())
    }

    override fun notifyFailed(msgId: Long) {
        val message = messageRepo.getMessage(msgId)

        if (message == null || !message.isFailedMessage()) {
            return
        }

        val conversation = conversationRepo.getConversation(message.threadId) ?: return
        val threadId = conversation.id

        val contentIntent = Intent(context, ComposeActivity::class.java).putExtra("threadId", threadId)
        val taskStackBuilder = TaskStackBuilder.create(context)
        taskStackBuilder.addParentStack(ComposeActivity::class.java)
        taskStackBuilder.addNextIntent(contentIntent)
        val contentPI = taskStackBuilder.getPendingIntent(threadId.toInt() + 40000, PendingIntent.FLAG_UPDATE_CURRENT)

        val notification = NotificationCompat.Builder(context, getChannelIdForNotification(threadId))
            .setContentTitle(context.getString(R.string.title_send_failed))
            .setContentText(context.getString(R.string.description_failed_to_send_to, conversation.getTitle()))
            .setColor(context.resolveThemeColor(R.attr.tintPrimary))
            .setPriority(NotificationManagerCompat.IMPORTANCE_MAX)
            .setSmallIcon(R.drawable.ic_notification_failed)
            .setAutoCancel(true)
            .setContentIntent(contentPI)
           // .setSound(Uri.parse(prefs.ringtone(threadId).get()))
            .setLights(Color.WHITE, 500, 2000)
            //.setVibrate(if (prefs.vibration(threadId).get()) VIBRATE_PATTERN else longArrayOf(0))

        notificationManager.notify(threadId.toInt() + 50000, notification.build())
    }

    /*
    private fun getReplyAction(threadId: Long): NotificationCompat.Action {

        val replyIntent = Intent(context, RemoteMessagingReceiver::class.java).putExtra("threadId", threadId)
        val replyPI = PendingIntent.getBroadcast(context, threadId.toInt() + 40000, replyIntent, PendingIntent.FLAG_UPDATE_CURRENT)

        val title = context.resources.getStringArray(R.array.notification_actions)[MyPreferences.NOTIFICATION_ACTION_REPLY]
        val responseSet = context.resources.getStringArray(R.array.qk_responses)
        val remoteInput = RemoteInput.Builder("body")
            .setLabel(title)

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.P) {
            remoteInput.setChoices(responseSet)
        }

        return NotificationCompat.Action.Builder(R.drawable.ic_reply_white_24dp, title, replyPI)
            .setSemanticAction(NotificationCompat.Action.SEMANTIC_ACTION_REPLY)
            .addRemoteInput(remoteInput.build())
            .build()

    }
    */

    /**
     * Creates a notification channel for the given conversation
     */
    override fun createNotificationChannel(threadId: Long) {

        // Only proceed if the android version supports notification channels
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return

        conversationRepo.getConversation(threadId)?.let { conversation ->
            val channelId = buildNotificationChannelId(threadId)
            val name = conversation.getTitle()
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(channelId, name, importance).apply {
                enableLights(true)
                lightColor = Color.WHITE
                enableVibration(true)
                vibrationPattern = VIBRATE_PATTERN
            }

            notificationManager.createNotificationChannel(channel)
        }
    }

    override fun createNotificationChannelForConnection(connectionId: Long) {
        // Only proceed if the android version supports notification channels
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return

        val channelId = buildNotificationChannelId(connectionId)
        val name = "Connection$connectionId"
        val importance = NotificationManager.IMPORTANCE_HIGH
        val channel = NotificationChannel(channelId, name, importance).apply {
            enableLights(true)
            lightColor = Color.WHITE
            enableVibration(true)
            vibrationPattern = VIBRATE_PATTERN
        }

        notificationManager.createNotificationChannel(channel)
    }


    /**
     * Returns the notification channel for the given conversation, or null if it doesn't exist
     */
    private fun getNotificationChannel(threadId: Long): NotificationChannel? {
        val channelId = buildNotificationChannelId(threadId)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            return notificationManager
                .notificationChannels
                .firstOrNull { channel -> channel.id == channelId }
        }

        return null
    }

    /**
     * Returns the channel id that should be used for a notification based on the threadId
     *
     * If a notification channel for the conversation exists, use the id for that. Otherwise return
     * the default channel id
     */
    private fun getChannelIdForNotification(threadId: Long): String {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channelId = buildNotificationChannelId(threadId)

            return notificationManager
                .notificationChannels
                .map { channel -> channel.id }
                .firstOrNull { id -> id == channelId }
                ?: DEFAULT_CHANNEL_ID
        }

        return DEFAULT_CHANNEL_ID
    }

    /**
     * Formats a notification channel id for a given thread id, whether the channel exists or not
     */
    override fun buildNotificationChannelId(threadId: Long): String {
        return when (threadId) {
            0L -> DEFAULT_CHANNEL_ID
            else -> "notifications_$threadId"
        }
    }

    override fun getNotificationForBackup(): NotificationCompat.Builder {
        if (Build.VERSION.SDK_INT >= 26) {
            val name = "Backup channel"
            val importance = NotificationManager.IMPORTANCE_LOW
            val channel = NotificationChannel(BACKUP_RESTORE_CHANNEL_ID, name, importance)
            val notificationManager = context.getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }

        return NotificationCompat.Builder(context, BACKUP_RESTORE_CHANNEL_ID)
            .setContentTitle("Backup")
            .setShowWhen(false)
            .setWhen(System.currentTimeMillis()) // Set this anyway in case it's shown
            .setSmallIcon(R.drawable.ic_file_download_black_24dp)
            .setColor(context.resolveThemeColor(R.attr.tintPrimary))
            .setCategory(NotificationCompat.CATEGORY_PROGRESS)
            .setPriority(NotificationCompat.PRIORITY_MIN)
            .setProgress(0, 0, true)
            .setOngoing(true)
    }


    companion object {
        const val DEFAULT_CHANNEL_ID = "notifications_default"
        const val BACKUP_RESTORE_CHANNEL_ID = "notifications_backup_restore"

        val VIBRATE_PATTERN = longArrayOf(0, 200, 0, 200)
    }
}