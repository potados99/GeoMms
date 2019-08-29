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
package com.potados.geomms.repository

import android.app.PendingIntent
import android.content.*
import android.provider.Telephony
import android.telephony.PhoneNumberUtils
import android.telephony.SmsManager
import com.google.android.mms.ContentType
import com.google.android.mms.MMSPart
import com.klinker.android.send_message.SmsManagerFactory
import com.klinker.android.send_message.Transaction
import com.potados.geomms.compat.TelephonyCompat
import com.potados.geomms.extension.anyOf
import com.potados.geomms.extension.nullOnFail
import com.potados.geomms.extension.tryOrNull
import com.potados.geomms.extension.unitOnFail
import com.potados.geomms.manager.ActiveConversationManager
import com.potados.geomms.manager.KeyManager
import com.potados.geomms.manager.KeyManagerImpl.Companion.CHANNEL_MESSAGE
import com.potados.geomms.model.Attachment
import com.potados.geomms.model.Conversation
import com.potados.geomms.model.Message
import com.potados.geomms.model.MmsPart
import com.potados.geomms.preference.MyPreferences
import com.potados.geomms.receiver.SmsDeliveredReceiver
import com.potados.geomms.receiver.SmsSentReceiver
import com.potados.geomms.util.ImageUtils
import io.realm.Case
import io.realm.Realm
import io.realm.RealmResults
import io.realm.Sort
import timber.log.Timber

class MessageRepositoryImpl(
    private val activeConversationManager: ActiveConversationManager,
    private val context: Context,
    private val messageIds: KeyManager,
    private val imageRepository: ImageRepository,
    private val prefs: MyPreferences,
    private val syncRepository: SyncRepository
) : MessageRepository() {

    override fun getMessages(threadId: Long, query: String): RealmResults<Message>? = nullOnFail {
         return@nullOnFail Realm.getDefaultInstance()
            .where(Message::class.java)
            .equalTo("threadId", threadId)
            .let { if (query.isEmpty()) it else it.contains("body", query, Case.INSENSITIVE) }
            .sort("date")
            .findAllAsync()
    }

    override fun getMessage(id: Long): Message? = nullOnFail {
         return@nullOnFail Realm.getDefaultInstance()
            .where(Message::class.java)
            .equalTo("id", id)
            .findFirst()
    }

    override fun getMessageForPart(id: Long): Message? = nullOnFail {
         return@nullOnFail Realm.getDefaultInstance()
            .where(Message::class.java)
            .equalTo("parts.id", id)
            .findFirst()
    }

    override fun getUnreadCount(): Long? = nullOnFail {
         return@nullOnFail Realm.getDefaultInstance()
            .where(Conversation::class.java)
            .equalTo("archived", false)
            .equalTo("blocked", false)
            .equalTo("read", false)
            .count()
    }

    override fun getPart(id: Long): MmsPart? = nullOnFail {
         return@nullOnFail Realm.getDefaultInstance()
            .where(MmsPart::class.java)
            .equalTo("id", id)
            .findFirst()
    }

    override fun getPartsForConversation(threadId: Long): RealmResults<MmsPart>? = nullOnFail {
         return@nullOnFail Realm.getDefaultInstance()
            .where(MmsPart::class.java)
            .equalTo("messages.threadId", threadId)
            .beginGroup()
            .contains("type", "image/")
            .or()
            .contains("type", "video/")
            .endGroup()
            .sort("id", Sort.DESCENDING)
            .findAllAsync()
    }

    /**
     * Retrieves the list of messages which should be shown in the notification
     * for a given conversation
     */
    override fun getUnreadUnseenMessages(threadId: Long): RealmResults<Message>? = nullOnFail {
         return@nullOnFail Realm.getDefaultInstance()
            .also { it.refresh() }
            .where(Message::class.java)
            .equalTo("seen", false)
            .equalTo("read", false)
            .equalTo("threadId", threadId)
            .sort("date")
            .findAll()
    }

    override fun getUnreadMessages(threadId: Long): RealmResults<Message>? = nullOnFail {
         return@nullOnFail Realm.getDefaultInstance()
            .where(Message::class.java)
            .equalTo("read", false)
            .equalTo("threadId", threadId)
            .sort("date")
            .findAll()
    }

    override fun markAllSeen() = unitOnFail {
        val realm = Realm.getDefaultInstance()
        val messages = realm.where(Message::class.java).equalTo("seen", false).findAll()
        realm.executeTransaction { messages.forEach { message -> message.seen = true } }
        realm.close()
    }

    override fun markSeen(threadId: Long) = unitOnFail {
        val realm = Realm.getDefaultInstance()
        val messages = realm.where(Message::class.java)
            .equalTo("threadId", threadId)
            .equalTo("seen", false)
            .findAll()

        realm.executeTransaction {
            messages.forEach { message ->
                message.seen = true
            }
        }
        realm.close()
    }

    override fun markRead(vararg threadIds: Long) = unitOnFail {
        /**
         * Realm 업데이트
         */
        Realm.getDefaultInstance()?.use { realm ->
            val messages = realm.where(Message::class.java)
                .anyOf("threadId", threadIds)
                .beginGroup()
                .equalTo("read", false)
                .or()
                .equalTo("seen", false)
                .endGroup()
                .findAll()

            realm.executeTransaction {
                messages.forEach { message ->
                    message.seen = true
                    message.read = true
                }
            }
        }

        /**
         * 네이티브 provider 업데이트
         */
        val values = ContentValues()
        values.put(Telephony.Sms.SEEN, true)
        values.put(Telephony.Sms.READ, true)

        threadIds.forEach { threadId ->
            try {
                val uri = ContentUris.withAppendedId(Telephony.MmsSms.CONTENT_CONVERSATIONS_URI, threadId)
                context.contentResolver.update(uri, values, "${Telephony.Sms.READ} = 0", null)
            } catch (e: Exception) {
                Timber.w(e)
            }
        }
    }

    override fun markUnread(vararg threadIds: Long) = unitOnFail {
        Realm.getDefaultInstance()?.use { realm ->
            val conversation = realm.where(Conversation::class.java)
                .anyOf("id", threadIds)
                .equalTo("read", true)
                .findAll()

            realm.executeTransaction {
                conversation.forEach { it.read = false }
            }
        }
    }

    override fun sendMessage(
        subId: Int,
        threadId: Long,
        addresses: List<String>,
        body: String,
        attachments: List<Attachment>
    ) = unitOnFail {
        if (addresses.size == 1 && attachments.isEmpty()) {
            // SMS
            insertSentSms(subId, threadId, addresses.first(), body, System.currentTimeMillis())?.let(::sendSms)
        } else {
            // MMS
            val parts = arrayListOf<MMSPart>()

            if (body.isNotBlank()) {
                parts += MMSPart("text", ContentType.TEXT_PLAIN, body.toByteArray())
            }

            // Add the GIFs as attachments
            parts += attachments
                .mapNotNull { attachment -> attachment as? Attachment.Image }
                .filter { attachment -> attachment.isGif(context) }
                .mapNotNull { attachment -> attachment.getUri() }
                .map { uri -> ImageUtils.compressGif(context, uri, 300 /* TODO */ * 1024) }
                .map { bitmap -> MMSPart("image", ContentType.IMAGE_GIF, bitmap) }

            // Compress the images and add them as attachments
            var totalImageBytes = 0
            parts += attachments
                .mapNotNull { attachment -> attachment as? Attachment.Image }
                .filter { attachment -> !attachment.isGif(context) }
                .mapNotNull { attachment -> attachment.getUri() }
                .mapNotNull { uri -> tryOrNull { imageRepository.loadImage(uri) } }
                .also { totalImageBytes = it.sumBy { it.allocationByteCount } }
                .map { bitmap ->
                    val byteRatio = bitmap.allocationByteCount / totalImageBytes.toFloat()
                    ImageUtils.compressBitmap(bitmap, (300 /* TODO */ * 1024 * byteRatio).toInt())
                }
                .map { bitmap -> MMSPart("image", ContentType.IMAGE_JPEG, bitmap) }

            // Send contacts
            parts += attachments
                .mapNotNull { attachment -> attachment as? Attachment.Contact }
                .map { attachment -> attachment.vCard.toByteArray() }
                .map { vCard -> MMSPart("contact", ContentType.TEXT_VCARD, vCard) }

            val transaction = Transaction(context)
            transaction.sendNewMessage(subId, threadId, addresses.map(PhoneNumberUtils::stripSeparators), parts, null)
        }
    }

    override fun sendSms(message: Message) = unitOnFail {
        val smsManager = message.subId.takeIf { it != -1 }
            ?.let(SmsManagerFactory::createSmsManager)
            ?: SmsManager.getDefault()

        val parts = smsManager.divideMessage(message.body) ?: arrayListOf()

        val sentIntents = parts.map {
            context.registerReceiver(SmsSentReceiver(), IntentFilter(SmsSentReceiver.ACTION))
            val intent = Intent(SmsSentReceiver.ACTION).putExtra("id", message.id)
            PendingIntent.getBroadcast(context, message.id.toInt(), intent, PendingIntent.FLAG_UPDATE_CURRENT)
        }

        val deliveredIntents = parts.map {
            context.registerReceiver(SmsDeliveredReceiver(), IntentFilter(SmsDeliveredReceiver.ACTION))
            val intent = Intent(SmsDeliveredReceiver.ACTION).putExtra("id", message.id)

            PendingIntent.getBroadcast(context, message.id.toInt(), intent, PendingIntent.FLAG_UPDATE_CURRENT)
        }

        try {
            smsManager.sendMultipartTextMessage(
                message.address,
                null,
                parts,
                ArrayList(sentIntents),
                ArrayList(deliveredIntents)
            )
        } catch (e: IllegalArgumentException) {
            Timber.w(e, "Message body lengths: ${parts.map { it.length }}")
            markFailed(message.id, Telephony.MmsSms.ERR_TYPE_GENERIC)
        }
    }

    override fun insertSentSms(subId: Int, threadId: Long, address: String, body: String, date: Long): Message? = nullOnFail {

        // Insert the message to Realm
        val message = Message().apply {
            this.threadId = threadId
            this.address = address
            this.body = body
            this.date = date
            this.subId = subId

            id = messageIds.newId(CHANNEL_MESSAGE)
            boxId = Telephony.Sms.MESSAGE_TYPE_OUTBOX
            type = "sms"
            read = true
            seen = true
        }
        val realm = Realm.getDefaultInstance()
        var managedMessage: Message? = null
        realm.executeTransaction { managedMessage = realm.copyToRealmOrUpdate(message) }

        // Insert the message to the native content provider
        val values = ContentValues().apply {
            put(Telephony.Sms.ADDRESS, address)
            put(Telephony.Sms.BODY, body)
            put(Telephony.Sms.DATE, System.currentTimeMillis())
            put(Telephony.Sms.READ, true)
            put(Telephony.Sms.SEEN, true)
            put(Telephony.Sms.TYPE, Telephony.Sms.MESSAGE_TYPE_OUTBOX)
            put(Telephony.Sms.THREAD_ID, threadId)
            put(Telephony.Sms.SUBSCRIPTION_ID, subId)
        }
        val uri = context.contentResolver.insert(Telephony.Sms.CONTENT_URI, values)

        // Update the contentId after the message has been inserted to the content provider
        // The message might have been deleted by now, so only proceed if it's valid
        //
        // We do this after inserting the message because it might be slow, and we want the message
        // to be inserted into Realm immediately. We don't need to do this after receiving one
        realm.executeTransaction { managedMessage?.takeIf { it.isValid }?.contentId = uri.lastPathSegment.toLong() }
        realm.close()

        // On some devices, we can't obtain a threadId until after the first message is sent in a
        // conversation. In this case, we need to update the message's threadId after it gets added
        // to the native ContentProvider
        if (threadId == 0L) {
            uri?.let(syncRepository::syncMessage)
        }

         return@nullOnFail message
    }

    override fun insertReceivedSms(subId: Int, address: String, body: String, sentTime: Long): Message? = nullOnFail {

        // Insert the message to Realm
        val message = Message().apply {
            this.address = address
            this.body = body
            this.dateSent = sentTime
            this.date = System.currentTimeMillis()
            this.subId = subId

            id = messageIds.newId(CHANNEL_MESSAGE)
            threadId = TelephonyCompat.getOrCreateThreadId(context, address)
            boxId = Telephony.Sms.MESSAGE_TYPE_INBOX
            type = "sms"
            read = activeConversationManager.getActiveConversation() == threadId
        }
        val realm = Realm.getDefaultInstance()
        var managedMessage: Message? = null
        realm.executeTransaction { managedMessage = realm.copyToRealmOrUpdate(message) }

        // Insert the message to the native content provider
        val values = ContentValues().apply {
            put(Telephony.Sms.ADDRESS, address)
            put(Telephony.Sms.BODY, body)
            put(Telephony.Sms.DATE_SENT, sentTime)
            put(Telephony.Sms.SUBSCRIPTION_ID, subId)
        }

        context.contentResolver.insert(Telephony.Sms.Inbox.CONTENT_URI, values)?.let { uri ->
            // Update the contentId after the message has been inserted to the content provider
            realm.executeTransaction { managedMessage?.contentId = uri.lastPathSegment.toLong() }
        }

        realm.close()

        return@nullOnFail message
    }

    /**
     * Marks the message as sending, in case we need to retry sending it
     */
    override fun markSending(id: Long) = unitOnFail {
        Realm.getDefaultInstance().use { realm ->
            realm.refresh()

            val message = realm.where(Message::class.java).equalTo("id", id).findFirst()
            message?.let {
                // Update the message in realm
                realm.executeTransaction {
                    message.boxId = Telephony.Sms.MESSAGE_TYPE_OUTBOX
                }

                // Update the message in the native ContentProvider
                val values = ContentValues()
                values.put(Telephony.Sms.TYPE, Telephony.Sms.MESSAGE_TYPE_OUTBOX)
                context.contentResolver.update(message.getUri(), values, null, null)
            }
        }
    }

    override fun markSent(id: Long) = unitOnFail {
        Realm.getDefaultInstance().use { realm ->
            realm.refresh()

            val message = realm.where(Message::class.java).equalTo("id", id).findFirst()
            message?.let {
                // Update the message in realm
                realm.executeTransaction {
                    message.boxId = Telephony.Sms.MESSAGE_TYPE_SENT
                }

                // Update the message in the native ContentProvider
                val values = ContentValues()
                values.put(Telephony.Sms.TYPE, Telephony.Sms.MESSAGE_TYPE_SENT)
                context.contentResolver.update(message.getUri(), values, null, null)
            }
        }
    }

    override fun markFailed(id: Long, resultCode: Int) = unitOnFail {
        Realm.getDefaultInstance().use { realm ->
            realm.refresh()

            val message = realm.where(Message::class.java).equalTo("id", id).findFirst()
            message?.let {
                // Update the message in realm
                realm.executeTransaction {
                    message.boxId = Telephony.Sms.MESSAGE_TYPE_FAILED
                    message.errorCode = resultCode
                }

                // Update the message in the native ContentProvider
                val values = ContentValues()
                values.put(Telephony.Sms.TYPE, Telephony.Sms.MESSAGE_TYPE_FAILED)
                values.put(Telephony.Sms.ERROR_CODE, resultCode)
                context.contentResolver.update(message.getUri(), values, null, null)
            }
        }
    }

    override fun markDelivered(id: Long) = unitOnFail {
        Realm.getDefaultInstance().use { realm ->
            realm.refresh()

            val message = realm.where(Message::class.java).equalTo("id", id).findFirst()
            message?.let {
                // Update the message in realm
                realm.executeTransaction {
                    message.deliveryStatus = Telephony.Sms.STATUS_COMPLETE
                    message.dateSent = System.currentTimeMillis()
                    message.read = true
                }

                // Update the message in the native ContentProvider
                val values = ContentValues()
                values.put(Telephony.Sms.STATUS, Telephony.Sms.STATUS_COMPLETE)
                values.put(Telephony.Sms.DATE_SENT, System.currentTimeMillis())
                values.put(Telephony.Sms.READ, true)
                context.contentResolver.update(message.getUri(), values, null, null)
            }
        }
    }

    override fun markDeliveryFailed(id: Long, resultCode: Int) = unitOnFail {
        Realm.getDefaultInstance().use { realm ->
            realm.refresh()

            val message = realm.where(Message::class.java).equalTo("id", id).findFirst()
            message?.let {
                // Update the message in realm
                realm.executeTransaction {
                    message.deliveryStatus = Telephony.Sms.STATUS_FAILED
                    message.dateSent = System.currentTimeMillis()
                    message.read = true
                    message.errorCode = resultCode
                }

                // Update the message in the native ContentProvider
                val values = ContentValues()
                values.put(Telephony.Sms.STATUS, Telephony.Sms.STATUS_FAILED)
                values.put(Telephony.Sms.DATE_SENT, System.currentTimeMillis())
                values.put(Telephony.Sms.READ, true)
                values.put(Telephony.Sms.ERROR_CODE, resultCode)
                context.contentResolver.update(message.getUri(), values, null, null)
            }
        }
    }

    override fun deleteMessages(vararg messageIds: Long) = unitOnFail {
        Realm.getDefaultInstance().use { realm ->
            realm.refresh()

            val messages = realm.where(Message::class.java)
                .anyOf("id", messageIds)
                .findAll()

            val uris = messages.map { it.getUri() }

            realm.executeTransaction { messages.deleteAllFromRealm() }

            uris.forEach { uri -> context.contentResolver.delete(uri, null, null) }
        }
    }

}