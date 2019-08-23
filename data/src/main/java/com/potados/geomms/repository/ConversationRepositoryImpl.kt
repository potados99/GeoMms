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

import android.content.ContentUris
import android.content.Context
import android.provider.Telephony
import android.telephony.PhoneNumberUtils
import com.potados.geomms.compat.TelephonyCompat
import com.potados.geomms.extension.anyOf
import com.potados.geomms.extension.map
import com.potados.geomms.model.Contact
import com.potados.geomms.model.Conversation
import com.potados.geomms.model.Message
import com.potados.geomms.extension.tryOrNull
import com.potados.geomms.mapper.CursorToConversation
import com.potados.geomms.mapper.CursorToRecipient
import io.realm.Realm
import io.realm.RealmResults
import io.realm.Sort
import java.util.concurrent.TimeUnit

class ConversationRepositoryImpl(
    private val context: Context,
    private val cursorToConversation: CursorToConversation,
    private val cursorToRecipient: CursorToRecipient
) : ConversationRepository {

    override fun getConversations(archived: Boolean): RealmResults<Conversation> {
        return Realm.getDefaultInstance()
            .where(Conversation::class.java)
            .notEqualTo("id", 0L)
            .greaterThan("count", 0)
            .equalTo("archived", archived)
            .equalTo("blocked", false)
            .isNotEmpty("recipients")
            .sort("pinned", Sort.DESCENDING, "date", Sort.DESCENDING)
            .findAllAsync()
    }

    override fun getConversationsSnapshot(): List<Conversation> {
        val realm = Realm.getDefaultInstance()
        return realm.copyFromRealm(
            realm.where(Conversation::class.java)
                .notEqualTo("id", 0L)
                .greaterThan("count", 0)
                .equalTo("archived", false)
                .equalTo("blocked", false)
                .isNotEmpty("recipients")
                .sort("pinned", Sort.DESCENDING, "date", Sort.DESCENDING)
                .findAll()
        )
    }

    override fun getTopConversations(): List<Conversation> {
        val realm = Realm.getDefaultInstance()
        return realm.copyFromRealm(
            realm.where(Conversation::class.java)
                .notEqualTo("id", 0L)
                .greaterThan("count", 0)
                .greaterThan("date", System.currentTimeMillis() - TimeUnit.DAYS.toMillis(7))
                .equalTo("archived", false)
                .equalTo("blocked", false)
                .isNotEmpty("recipients")
                .sort("pinned", Sort.DESCENDING, "count", Sort.DESCENDING)
                .findAll()
        )
    }

    override fun setConversationName(id: Long, name: String) {
        Realm.getDefaultInstance().use { realm ->
            realm.executeTransaction {
                realm.where(Conversation::class.java)
                    .equalTo("id", id)
                    .findFirst()
                    ?.name = name
            }
        }
    }

    override fun getBlockedConversations(): RealmResults<Conversation> {
        return Realm.getDefaultInstance()
            .where(Conversation::class.java)
            .equalTo("blocked", true)
            .findAllAsync()
    }

    override fun getConversationAsync(threadId: Long): Conversation {
        return Realm.getDefaultInstance()
            .where(Conversation::class.java)
            .equalTo("id", threadId)
            .findFirstAsync()
    }

    override fun getConversation(threadId: Long): Conversation? {
        return Realm.getDefaultInstance()
            .where(Conversation::class.java)
            .equalTo("id", threadId)
            .findFirst()
    }

    override fun getThreadId(recipient: String): Long? {
        return getThreadId(listOf(recipient))
    }

    override fun getThreadId(recipients: Collection<String>): Long? {
        return Realm.getDefaultInstance().use { realm ->
            realm.where(Conversation::class.java)
                .findAll()
                .firstOrNull { conversation ->
                    conversation.recipients.size == recipients.size && conversation.recipients.map { it.address }.all { address ->
                        recipients.any { recipient -> PhoneNumberUtils.compare(recipient, address) }
                    }
                }
                ?.id
        }
    }

    override fun getOrCreateConversation(threadId: Long): Conversation? {
        return getConversation(threadId) ?: updateConversationFromCp(threadId)
    }

    override fun getOrCreateConversation(address: String): Conversation? {
        return getOrCreateConversation(listOf(address))
    }

    override fun getOrCreateConversation(addresses: List<String>): Conversation? {
        return tryOrNull {
            TelephonyCompat.getOrCreateThreadId(
                context,
                addresses.toSet()
            )
        }
            ?.takeIf { threadId -> threadId != 0L }
            ?.let { threadId ->
                var conversation = getConversation(threadId)

                if (conversation != null) {
                    // get unmanaged copy
                    conversation = Realm.getDefaultInstance().copyFromRealm(conversation)
                }

                conversation ?: updateConversationFromCp(threadId)
            }
    }

    override fun saveDraft(threadId: Long, draft: String) {
        Realm.getDefaultInstance().use { realm ->
            realm.refresh()

            val conversation = realm.where(Conversation::class.java)
                .equalTo("id", threadId)
                .findFirst()

            realm.executeTransaction {
                conversation?.takeIf { it.isValid }?.draft = draft
            }
        }
    }

    override fun updateConversations(vararg threadIds: Long) {
        Realm.getDefaultInstance().use { realm ->
            realm.refresh()

            threadIds.forEach { threadId ->
                val conversation = realm
                    .where(Conversation::class.java)
                    .equalTo("id", threadId)
                    .findFirst() ?: return

                val messages = realm
                    .where(Message::class.java)
                    .equalTo("threadId", threadId)
                    .sort("date", Sort.DESCENDING)
                    .findAll()

                val message = messages.firstOrNull()

                realm.executeTransaction {
                    conversation.count = messages.size
                    conversation.date = message?.date ?: 0
                    conversation.snippet = message?.getSummary() ?: ""
                    conversation.read = message?.read ?: true
                    conversation.me = message?.isMe() ?: false
                }
            }
        }
    }

    override fun markArchived(vararg threadIds: Long) {
        Realm.getDefaultInstance().use { realm ->
            val conversations = realm.where(Conversation::class.java)
                .anyOf("id", threadIds)
                .findAll()

            realm.executeTransaction {
                conversations.forEach { it.archived = true }
            }
        }
    }

    override fun markUnarchived(vararg threadIds: Long) {
        Realm.getDefaultInstance().use { realm ->
            val conversations = realm.where(Conversation::class.java)
                .anyOf("id", threadIds)
                .findAll()

            realm.executeTransaction {
                conversations.forEach { it.archived = false }
            }
        }
    }

    override fun markPinned(vararg threadIds: Long) {
        Realm.getDefaultInstance().use { realm ->
            val conversations = realm.where(Conversation::class.java)
                .anyOf("id", threadIds)
                .findAll()

            realm.executeTransaction {
                conversations.forEach { it.pinned = true }
            }
        }
    }

    override fun markUnpinned(vararg threadIds: Long) {
        Realm.getDefaultInstance().use { realm ->
            val conversations = realm.where(Conversation::class.java)
                .anyOf("id", threadIds)
                .findAll()

            realm.executeTransaction {
                conversations.forEach { it.pinned = false }
            }
        }
    }

    override fun markBlocked(vararg threadIds: Long) {
        Realm.getDefaultInstance().use { realm ->
            val conversations = realm.where(Conversation::class.java)
                .anyOf("id", threadIds)
                .findAll()

            realm.executeTransaction {
                conversations.forEach { it.blocked = true }
            }
        }
    }

    override fun markUnblocked(vararg threadIds: Long) {
        Realm.getDefaultInstance().use { realm ->
            val conversations = realm.where(Conversation::class.java)
                .anyOf("id", threadIds)
                .findAll()

            realm.executeTransaction {
                conversations.forEach { it.blocked = false }
            }
        }
    }

    override fun deleteConversations(vararg threadIds: Long) {
        Realm.getDefaultInstance().use { realm ->
            val conversation = realm.where(Conversation::class.java).anyOf("id", threadIds).findAll()
            val messages = realm.where(Message::class.java).anyOf("threadId", threadIds).findAll()

            realm.executeTransaction {
                conversation.deleteAllFromRealm()
                messages.deleteAllFromRealm()
            }
        }

        threadIds.forEach { threadId ->
            val uri = ContentUris.withAppendedId(Telephony.Threads.CONTENT_URI, threadId)
            context.contentResolver.delete(uri, null, null)
        }
    }

    /**
     * Returns a [Conversation] from the system SMS ContentProvider, based on the [threadId]
     *
     * It should be noted that even if we have a valid [threadId], that does not guarantee that
     * we can return a [Conversation]. On some devices, the ContentProvider won't return the
     * conversation unless it contains at least 1 message
     */
    private fun updateConversationFromCp(threadId: Long): Conversation? {
        return cursorToConversation.getConversationsCursor()
            ?.map(cursorToConversation::map)
            ?.firstOrNull { it.id == threadId }
            ?.let { conversation ->
                val realm = Realm.getDefaultInstance()
                val contacts = realm.copyFromRealm(realm.where(Contact::class.java).findAll())

                val recipients = conversation.recipients
                    .map { recipient -> recipient.id }
                    .map { id -> cursorToRecipient.getRecipientCursor(id) }
                    .mapNotNull { recipientCursor ->
                        // Map the recipient cursor to a list of recipients
                        recipientCursor?.use { recipientCursor.map { cursorToRecipient.map(recipientCursor) } }
                    }
                    .flatten()
                    .map { recipient ->
                        recipient.apply {
                            contact = contacts.firstOrNull {
                                it.numbers.any { PhoneNumberUtils.compare(recipient.address, it.address) }
                            }
                        }
                    }

                conversation.recipients.clear()
                conversation.recipients.addAll(recipients)
                realm.executeTransaction { it.insertOrUpdate(conversation) }
                realm.close()

                conversation
            }
    }

}