/*
 * Copyright (C) 2019 Song Byeong Jun <potados99@gmail.com>
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

package com.potados.geomms.repository

import android.content.ContentResolver
import android.content.ContentUris
import android.net.Uri
import android.provider.Telephony
import android.telephony.PhoneNumberUtils
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.potados.geomms.extension.*
import com.potados.geomms.manager.KeyManager
import com.potados.geomms.manager.KeyManagerImpl.Companion.CHANNEL_MESSAGE
import com.potados.geomms.mapper.CursorToContact
import com.potados.geomms.mapper.CursorToConversation
import com.potados.geomms.mapper.CursorToMessage
import com.potados.geomms.mapper.CursorToRecipient
import com.potados.geomms.model.*
import io.realm.Realm
import io.realm.Sort
import timber.log.Timber

class SyncRepositoryImpl(
    private val contentResolver: ContentResolver,
    private val conversationRepo: ConversationRepository,
    private val cursorToConversation: CursorToConversation,
    private val cursorToMessage: CursorToMessage,
    private val cursorToRecipient: CursorToRecipient,
    private val cursorToContact: CursorToContact,
    private val keys: KeyManager
) : SyncRepository() {

    /**
     * Holds data that should be persisted across full syncs
     */
    private data class PersistedData(
        val id: Long,
        val archived: Boolean,
        val blocked: Boolean,
        val pinned: Boolean,
        val name: String
    )

    private val _progress = MutableLiveData<SyncProgress>().apply {
        value = SyncProgress.Idle
    }

    override val syncProgress: LiveData<SyncProgress> = _progress

    override val rows: Int
        get() {
            return (cursorToMessage.getMessagesCursor()?.count ?: 0) +
                    (cursorToMessage.getMessagesCursor()?.count ?: 0) +
                    (cursorToRecipient.getRecipientCursor()?.count ?: 0)
        }

    override fun syncMessages(dateFrom: Long) = unitOnFail {
        if (_progress.value is SyncProgress.Running) {
            Timber.i("sync already in progress; return")
            return@unitOnFail
        }

        _progress.postValue(SyncProgress.Running(0, 0, true))

        val realm = Realm.getDefaultInstance()
        realm.beginTransaction()

        Timber.i("Realm transaction began")

        val persistedData = realm.where(Conversation::class.java)
                .beginGroup()
                .equalTo("archived", true)
                .or()
                .equalTo("blocked", true)
                .or()
                .equalTo("pinned", true)
                .or()
                .isNotEmpty("name")
                .endGroup()
                .findAll()
                .map { PersistedData(it.id, it.archived, it.blocked, it.pinned, it.name) }

        realm.delete(Contact::class.java)
        realm.delete(Conversation::class.java)
        realm.delete(Message::class.java)
        realm.delete(MmsPart::class.java)
        realm.delete(Recipient::class.java)

        keys.reset(CHANNEL_MESSAGE)

        Timber.v("Removed everything except persistedData")

        Timber.i("Will sync messages and conversations after date $dateFrom")

        // Apply date filter here.
        // Only messages and conversations after the [dateFrom] will be queried.
        val messageCursor = cursorToMessage.getMessagesCursor(dateFrom)
        val conversationCursor = cursorToConversation.getConversationsCursor(dateFrom)
        val recipientCursor = cursorToRecipient.getRecipientCursor()

        val max = (messageCursor?.count ?: 0) +
                (conversationCursor?.count ?: 0) +
                (recipientCursor?.count ?: 0)

        Timber.v("Sum of cursor rows: $max")

        var progress = 0

        /**
         * Message
         */
        messageCursor?.use {
            val messageColumns = CursorToMessage.MessageColumns(messageCursor)
            val messages = messageCursor.map { cursor ->
                Timber.v("Syncing messages...$progress")

                progress++
                _progress.postValue(SyncProgress.Running(max, progress, false))
                cursorToMessage.map(Pair(cursor, messageColumns))
            }
            realm.insertOrUpdate(messages)
            Timber.i("Messages inserted to realm.")
        }

        /**
         * Conversation
         */
        conversationCursor?.use {
            val conversations = conversationCursor
                    .map { cursor ->
                        Timber.v("Syncing conversations...$progress")
                        postProgress(max, ++progress, false)
                        cursorToConversation.map(cursor)
                    }

            persistedData.forEach { data ->
                val conversation = conversations.firstOrNull { conversation -> conversation.id == data.id }
                conversation?.archived = data.archived
                conversation?.blocked = data.blocked
                conversation?.pinned = data.pinned
                conversation?.name = data.name
            }

            Timber.i("Applied persisted data to conversations.")

            realm.where(Message::class.java)
                    .sort("date", Sort.DESCENDING)
                    .distinct("threadId")
                    .findAll()
                    .forEach { message ->
                        conversations
                            .firstOrNull { conversation -> conversation.id == message.threadId }
                            ?.apply { date = message.date }
                            ?.apply { snippet = message.getSummary() }
                            ?.apply { me = message.isMe() }

                        Timber.v("Updating conversations using latest messages.")
                    }

            realm.insertOrUpdate(conversations)
            Timber.i("Conversations inserted to realm.")
        }

        /**
         * Recipient
         *
         * Fill contacts in the recipients of the conversations.
         */
        recipientCursor?.use {
            val contacts = realm.copyToRealm(getContacts() as MutableList)
            val recipients = recipientCursor.map { cursor ->
                Timber.v("Syncing recipients...$progress")
                postProgress(max, ++progress, false)

                cursorToRecipient.map(cursor).apply {
                    contact = contacts.firstOrNull { contact ->
                        contact.numbers.any { PhoneNumberUtils.compare(address, it.address) }
                    }
                }
            }

            realm.insertOrUpdate(recipients)

            Timber.i("Recipients inserted to realm")
        }

        postProgress(0, 0, false)

        realm.insert(SyncLog())
        realm.commitTransaction()
        realm.close()

        Timber.i("Szync finished")

        postIdle()
    }

    override fun syncMessage(uri: Uri): Message? = nullOnFail {

        // If we don't have a valid type, return null
        val type = when {
            uri.toString().contains("mms") -> "mms"
            uri.toString().contains("sms") -> "sms"
            else -> return@nullOnFail null
        }

        // If we don't have a valid id, return null
        val id = tryOrNull(false) { ContentUris.parseId(uri) } ?: throw RuntimeException("Failed to sync message. No valid id for given uri.")

        // Check if the message already exists, so we can reuse the id
        val existingId = Realm.getDefaultInstance().use { realm ->
            realm.refresh()
            realm.where(Message::class.java)
                    .equalTo("type", type)
                    .equalTo("contentId", id)
                    .findFirst()
                    ?.id
        }

        // The uri might be something like content://mms/inbox/id
        // The box might change though, so we should just use the mms/id uri
        val stableUri = when (type) {
            "mms" -> ContentUris.withAppendedId(Telephony.Mms.CONTENT_URI, id)
            else -> ContentUris.withAppendedId(Telephony.Sms.CONTENT_URI, id)
        }

        return@nullOnFail contentResolver.query(stableUri, null, null, null, null)?.use { cursor ->

            // If there are no rows, return null. Otherwise, we've moved to the first row
            if (!cursor.moveToFirst()) return@nullOnFail null

            val columnsMap = CursorToMessage.MessageColumns(cursor)
            cursorToMessage.map(Pair(cursor, columnsMap)).apply {
                existingId?.let { this.id = it }

                conversationRepo.getOrCreateConversation(threadId)
                insertOrUpdate()
            }
        }
    }

    override fun syncContacts() = unitOnFail {
        // Load all the contacts
        var contacts = getContacts() ?: throw RuntimeException("Failed to sync contacts.")

        Realm.getDefaultInstance()?.use { realm ->
            val recipients = realm.where(Recipient::class.java).findAll()

            realm.executeTransaction {
                realm.delete(Contact::class.java)

                contacts = realm.copyToRealm(contacts)

                recipients.forEach {recipient ->
                    recipient.contact = contacts.find { contact ->
                        contact.numbers.any { PhoneNumberUtils.compare(recipient.address, it.address) }
                    }
                }
            }
        }
    }

    override fun syncContact(address: String): Boolean? = nullOnFail {
        // See if there's a contact that matches this phone number
        var contact = getContacts()?.firstOrNull {
            it.numbers.any { number -> PhoneNumberUtils.compare(number.address, address) }
        } ?: return@nullOnFail false

        Realm.getDefaultInstance().use { realm ->
            val recipients = realm.where(Recipient::class.java).findAll()

            realm.executeTransaction {
                contact = realm.copyToRealmOrUpdate(contact)

                // Update all the matching recipients with the new contact
                val updatedRecipients = recipients
                        .filter { recipient ->
                            contact.numbers.any { number ->
                                PhoneNumberUtils.compare(recipient.address, number.address)
                            }
                        }
                        .map { recipient -> recipient.apply { this.contact = contact } }

                realm.insertOrUpdate(updatedRecipients)
            }
        }

        return@nullOnFail true
    }

    private fun getContacts(): List<Contact>? = nullOnFail {
        return@nullOnFail cursorToContact.getContactsCursor()
                ?.map { cursor -> cursorToContact.map(cursor) }
                ?.groupBy { contact -> contact.lookupKey }
                ?.map { contacts -> // lookupKey에 의한 그룹. Map.Entry<String, Contact>
                    /**
                     * 같은 Lookup key를 가진 연락처는 같은 연락처이므로,
                     * lookup key를 공유하는 하나의 그룹은 첫 번째 value를
                     * 대표로 하여 축약합니다.
                     */
                    val allNumbers = contacts.value.map { it.numbers }.flatten()
                    contacts.value.first().apply {
                        numbers.clear()
                        numbers.addAll(allNumbers)
                    }
                } ?: listOf()
    }

    private fun postProgress(max: Int, progress: Int, indeterminate: Boolean) = unitOnFail {
        _progress.postValue(SyncProgress.Running(max, progress, indeterminate))
    }
    private fun postIdle() = unitOnFail {
        _progress.postValue(SyncProgress.Idle)
    }

}