/**
 * Copyright (C) 2019 Song Byeong Jun and original authors
 *
 * This file is part of GeoMms.
 *
 * This software makes use of third-party patent which belongs to
 * KANG MOON KYOU and LEE GWI BONG:
 * System and Method for sharing service of location information
 * 10-1235884-0000 (2013.02.15)
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

import com.potados.geomms.model.Conversation
import com.potados.geomms.model.SearchResult
import io.realm.RealmResults

abstract class ConversationRepository : Repository() {

    abstract fun getConversations(archived: Boolean = false): RealmResults<Conversation>?

    abstract fun getUnmanagedConversations(): List<Conversation>?

    /**
     * Returns the top conversations that were active in the last week
     */
    abstract fun getTopConversations(): List<Conversation>?

    abstract fun setConversationName(id: Long, name: String)

    abstract fun getBlockedConversations(): RealmResults<Conversation>?

    abstract fun searchConversations(query: String): List<SearchResult>?

    abstract fun getConversationAsync(threadId: Long): Conversation?

    abstract fun getConversation(threadId: Long): Conversation?

    abstract fun getThreadId(recipient: String): Long?

    abstract fun getThreadId(recipients: Collection<String>): Long?

    abstract fun getOrCreateConversation(threadId: Long): Conversation?

    abstract fun getOrCreateConversation(address: String): Conversation?

    abstract fun getOrCreateConversation(addresses: List<String>): Conversation?

    abstract fun saveDraft(threadId: Long, draft: String)

    /**
     * Updates message-related fields in the conversation, like the date and snippet
     */
    abstract fun updateConversations(vararg threadIds: Long)

    abstract fun markArchived(vararg threadIds: Long)

    abstract fun markUnarchived(vararg threadIds: Long)

    abstract fun markPinned(vararg threadIds: Long)

    abstract fun markUnpinned(vararg threadIds: Long)

    abstract fun markBlocked(vararg threadIds: Long)

    abstract fun markUnblocked(vararg threadIds: Long)

    abstract fun deleteConversations(vararg threadIds: Long)

}