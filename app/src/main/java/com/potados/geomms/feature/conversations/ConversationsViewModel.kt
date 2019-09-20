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
import com.potados.geomms.usecase.SyncMessages
import com.potados.geomms.util.Notify
import com.potados.geomms.util.Popup
import io.realm.Realm
import org.koin.core.KoinComponent
import org.koin.core.inject
import timber.log.Timber
import java.util.*

/**
 * View Model of [ConversationsFragment].
 *
 * @see [ConversationsFragment]
 */
class ConversationsViewModel : BaseViewModel(), KoinComponent {

    private val syncMessages: SyncMessages by inject()
    private val deleteConversations: DeleteConversations by inject()

    private val conversationRepo: ConversationRepository by inject()
    private val syncRepo: SyncRepository by inject()

    private val permissionManager: PermissionManager by inject()
    private val navigator: Navigator by inject()

    val syncEvent = syncRepo.syncEvent()

    /**
     * Binding properties.
     */
    val conversations = conversationRepo.getConversations()
    val searchResults = MutableLiveData<List<SearchResult>>()
    val defaultSmsState = permissionManager.isDefaultSmsLiveData()
    val syncState = syncRepo.syncProgress
    val searching = MutableLiveData<Boolean>().apply { value = false }

    init {
        failables += this
        failables += conversationRepo
        failables += syncRepo
        failables += permissionManager
    }

    override fun start() {
        super.start()
        sync()
    }

    /**
     * Sync messages on condition.
     */
    private fun sync() {
        val lastSync = Realm.getDefaultInstance().use { realm -> realm.where(SyncLog::class.java)?.max("date") ?: 0 }
        if (lastSync == 0 && permissionManager.isDefaultSms() && permissionManager.hasReadSms() && permissionManager.hasContacts()) {
            syncRepo.triggerSyncMessages()
        }
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
     * This method is recommended to be invoked by its owner childFragment,
     * in response of [syncEvent].
     *
     * This is supposed to be the only entry for sync in this whole application.
     */
    fun showSyncDialog(activity: FragmentActivity?) {
        navigator.showSyncDialog(activity)
    }

    /**
     * Update value of current default sms app in permission manager.
     */
    fun refresh() {
        permissionManager.refresh()
    }
}