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

import android.content.ContentResolver
import android.content.ContentUris
import android.net.Uri
import android.provider.Telephony
import android.telephony.PhoneNumberUtils
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.f2prateek.rx.preferences2.RxSharedPreferences
import com.potados.geomms.extension.insertOrUpdate
import com.potados.geomms.extension.map
import com.potados.geomms.extension.tryOrNull
import com.potados.geomms.manager.KeyManager
import com.potados.geomms.mapper.CursorToContact
import com.potados.geomms.mapper.CursorToConversation
import com.potados.geomms.mapper.CursorToMessage
import com.potados.geomms.mapper.CursorToRecipient
import com.potados.geomms.model.*
import io.reactivex.subjects.BehaviorSubject
import io.reactivex.subjects.Subject
import io.realm.Realm
import io.realm.Sort

class SyncRepositoryImpl(
    private val contentResolver: ContentResolver,
    private val conversationRepo: ConversationRepository,
    private val cursorToConversation: CursorToConversation,
    private val cursorToMessage: CursorToMessage,
    private val cursorToRecipient: CursorToRecipient,
    private val cursorToContact: CursorToContact,
    private val keys: KeyManager
) : SyncRepository {

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

    private val _progress = MutableLiveData<SyncRepository.SyncProgress>().apply {
        value = SyncRepository.SyncProgress.Idle()
    }

    override val syncProgress: Subject<SyncRepository.SyncProgress> =
        BehaviorSubject.createDefault(SyncRepository.SyncProgress.Idle())

    override fun syncMessages() {

        // If the sync is already running, don't try to do another one
        if (_progress.value is SyncRepository.SyncProgress.Running) return
        _progress.value = SyncRepository.SyncProgress.Running(0, 0, true)

        val realm = Realm.getDefaultInstance()
        realm.beginTransaction()

        var persistedData = realm.where(Conversation::class.java)
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

        keys.reset()

        val messageCursor = cursorToMessage.getMessagesCursor()
        val conversationCursor = cursorToConversation.getConversationsCursor()
        val recipientCursor = cursorToRecipient.getRecipientCursor()

        val max = (messageCursor?.count ?: 0) +
                (conversationCursor?.count ?: 0) +
                (recipientCursor?.count ?: 0)

        var progress = 0

        // Sync messages
        messageCursor?.use {
            val messageColumns = CursorToMessage.MessageColumns(messageCursor)
            val messages = messageCursor.map { cursor ->
                progress++
                _progress.value = SyncRepository.SyncProgress.Running(max, progress, false)
                cursorToMessage.map(Pair(cursor, messageColumns))
            }
            realm.insertOrUpdate(messages)
        }

        // Sync conversations
        conversationCursor?.use {
            val conversations = conversationCursor
                    .map { cursor ->
                        progress++
                        _progress.value = SyncRepository.SyncProgress.Running(max, progress, false)
                        cursorToConversation.map(cursor)
                    }

            persistedData.forEach { data ->
                val conversation = conversations.firstOrNull { conversation -> conversation.id == data.id }
                conversation?.archived = data.archived
                conversation?.blocked = data.blocked
                conversation?.pinned = data.pinned
                conversation?.name = data.name
            }

            realm.where(Message::class.java)
                    .sort("date", Sort.DESCENDING)
                    .distinct("threadId")
                    .findAll()
                    .forEach { message ->
                        val conversation = conversations
                                .firstOrNull { conversation -> conversation.id == message.threadId }
                        conversation?.date = message.date
                        conversation?.snippet = message.getSummary()
                        conversation?.me = message.isMe()
                    }

            realm.insertOrUpdate(conversations)
        }

        // Sync recipients
        recipientCursor?.use {
            val contacts = realm.copyToRealm(getContacts())
            val recipients = recipientCursor
                    .map { cursor ->
                        progress++
                        _progress.value = SyncRepository.SyncProgress.Running(max, progress, false)
                        cursorToRecipient.map(cursor).apply {
                            contact = contacts.firstOrNull { contact ->
                                contact.numbers.any { PhoneNumberUtils.compare(address, it.address) }
                            }
                        }
                    }
            realm.insertOrUpdate(recipients)
        }

        _progress.value = SyncRepository.SyncProgress.Running(0, 0, true)


        realm.insert(SyncLog())
        realm.commitTransaction()
        realm.close()

        _progress.value = SyncRepository.SyncProgress.Idle()
    }

    override fun syncMessage(uri: Uri): Message? {

        // If we don't have a valid type, return null
        val type = when {
            uri.toString().contains("mms") -> "mms"
            uri.toString().contains("sms") -> "sms"
            else -> return null
        }

        // If we don't have a valid id, return null
        val id = tryOrNull(false) { ContentUris.parseId(uri) } ?: return null

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

        return contentResolver.query(stableUri, null, null, null, null)?.use { cursor ->

            // If there are no rows, return null. Otherwise, we've moved to the first row
            if (!cursor.moveToFirst()) return null

            val columnsMap = CursorToMessage.MessageColumns(cursor)
            cursorToMessage.map(Pair(cursor, columnsMap)).apply {
                existingId?.let { this.id = it }

                conversationRepo.getOrCreateConversation(threadId)
                insertOrUpdate()
            }
        }
    }

    override fun syncContacts() {
        // Load all the contacts
        var contacts = getContacts()

        Realm.getDefaultInstance()?.use { realm ->
            val recipients = realm.where(Recipient::class.java).findAll()

            realm.executeTransaction {
                realm.delete(Contact::class.java)

                contacts = realm.copyToRealm(contacts)

                // Update all the recipients with the new contacts
                val updatedRecipients = recipients.map { recipient ->
                    recipient.apply {
                        contact = contacts.firstOrNull {
                            it.numbers.any { PhoneNumberUtils.compare(recipient.address, it.address) }
                        }
                    }
                }

                realm.insertOrUpdate(updatedRecipients)
            }

        }
    }

    override fun syncContact(address: String): Boolean {
        // See if there's a contact that matches this phone number
        var contact = getContacts().firstOrNull {
            it.numbers.any { number -> PhoneNumberUtils.compare(number.address, address) }
        } ?: return false

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

        return true
    }

    private fun getContacts(): List<Contact> {
        return cursorToContact.getContactsCursor()
                ?.map { cursor -> cursorToContact.map(cursor) }
                ?.groupBy { contact -> contact.lookupKey }
                ?.map { contacts ->
                    val allNumbers = contacts.value.map { it.numbers }.flatten()
                    contacts.value.first().apply {
                        numbers.clear()
                        numbers.addAll(allNumbers)
                    }
                } ?: listOf()
    }

}