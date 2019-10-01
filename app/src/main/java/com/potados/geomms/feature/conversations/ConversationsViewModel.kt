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

package com.potados.geomms.feature.conversations

import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.MutableLiveData
import com.potados.geomms.R
import com.potados.geomms.common.base.BaseViewModel
import com.potados.geomms.common.navigation.Navigator
import com.potados.geomms.manager.PermissionManager
import com.potados.geomms.model.Conversation
import com.potados.geomms.model.SearchResult
import com.potados.geomms.model.SyncLog
import com.potados.geomms.repository.ConversationRepository
import com.potados.geomms.repository.SyncRepository
import com.potados.geomms.usecase.DeleteConversations
import com.potados.geomms.util.Popup
import io.realm.Realm
import org.koin.core.KoinComponent
import org.koin.core.inject
import timber.log.Timber

/**
 * View Model of [ConversationsFragment].
 *
 * @see [ConversationsFragment]
 */
class ConversationsViewModel : BaseViewModel(), KoinComponent {

    private val deleteConversations: DeleteConversations by inject()

    private val conversationRepo: ConversationRepository by inject()
    private val syncRepo: SyncRepository by inject()

    private val permissionManager: PermissionManager by inject()
    private val navigator: Navigator by inject()

    /**
     * Binding properties.
     */
    val conversations = conversationRepo.getConversations()
    val searchResults = MutableLiveData<List<SearchResult>>()
    val searching = MutableLiveData<Boolean>().apply { value = false }

    init {
        failables += this
        failables += conversationRepo
        failables += syncRepo
        failables += permissionManager
    }

    fun showCompose() {
        navigator.showCompose()
    }

    fun showConversation(searchResult: SearchResult) {
        with(searchResult) {
            navigator.showConversation(conversation.id, query.takeIf { messages > 0 })
        }
    }

    fun showConversation(conversation: Conversation) {
        navigator.showConversation(conversation.id)
    }

    fun showConversationDeletionConfirmation(activity: FragmentActivity?, conversation: Conversation) {
        Popup(activity)
            .withTitle(R.string.title_delete_conversation)
            .withMessage(R.string.delete_conversation_message, conversation.getTitle())
            .withPositiveButton(R.string.button_delete) { deleteConversations(listOf(conversation.id)) }
            .withNegativeButton(R.string.button_cancel)
            .show()
    }

    fun changeDefaultSmsApp() {
        navigator.showDefaultSmsDialogIfNeeded()
    }

    /**
     * Set conversation based on query.
     *
     * @param query if null, show normal conversation list.
     */
    fun typeQuery(query: String?) {
        if (query.isNullOrEmpty()) {
            Timber.i("Null or empty query.")
            searching.value = false
        } else {
            Timber.i("Query typed: [$query]")
            searching.value = true
            searchResults.value = conversationRepo.searchConversations(query)
        }
    }

    /**
     * Update value of current default sms app in permission manager.
     */
    fun refresh() {
        permissionManager.refresh()
    }
}