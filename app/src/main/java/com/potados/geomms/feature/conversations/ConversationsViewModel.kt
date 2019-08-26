package com.potados.geomms.feature.conversations

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.potados.geomms.base.Failable
import com.potados.geomms.common.base.BaseViewModel
import com.potados.geomms.functional.Result
import com.potados.geomms.manager.PermissionManager
import com.potados.geomms.model.Conversation
import com.potados.geomms.model.SearchResult
import com.potados.geomms.model.SyncLog
import com.potados.geomms.repository.ConversationRepository
import com.potados.geomms.repository.SyncRepository
import com.potados.geomms.usecase.SyncMessages
import io.realm.Realm
import io.realm.RealmList
import io.realm.RealmResults
import org.koin.core.KoinComponent
import org.koin.core.inject
import timber.log.Timber

/**
 * View Model of [ConversationsFragment].
 *
 * @see [ConversationsFragment]
 */
class ConversationsViewModel : BaseViewModel(), KoinComponent {

    private val syncMessages: SyncMessages by inject()

    private val conversationRepo: ConversationRepository by inject()
    private val syncRepo: SyncRepository by inject()

    private val permissionManager: PermissionManager by inject()

    /**
     * Binding element.
     * Target: [conversations_fragment.xml]
     * @see [ConversationsBinding.kt]
     */
    val conversations = conversationRepo.getConversations()

    /**
     * Binding element
     * Target: [conversations_fragment.xml]
     * @see [ConversationsBinding.kt]
     */
    val searchResults = MutableLiveData<List<SearchResult>>()

    /**
     * Binding element.
     * Target: [main_hint.xml]
     * @see [ConversationsBinding.kt]
     */
    val defaultSmsState = permissionManager.isDefaultSmsLiveData()

    /**
     * Binding element
     * Target: [main_syncing.xml]
     * @see [ConversationsBinding.kt]
     */
    val syncState = syncRepo.syncProgress

    val searching = MutableLiveData<Boolean>().apply { value = false }

    override fun start() {
        super.start()

        failables.addAll(
            listOf(
                this,
                conversationRepo,
                syncRepo,
                permissionManager
            )
        )

        sync()
    }

    /**
     * Sync messages on condition.
     */
    private fun sync() {
        // If we have all permissions and we've never run a sync, run a sync. This will be the case
        // when upgrading from 2.7.3, or if the app's data was cleared
        val lastSync = Realm.getDefaultInstance().use { realm -> realm.where(SyncLog::class.java)?.max("date") ?: 0 }
        if (lastSync == 0 && permissionManager.isDefaultSms() && permissionManager.hasReadSms() && permissionManager.hasContacts()) {
            syncMessages(Unit) {
                if (it is Result.Error) {
                    setFailure(Failable.Failure("Failed to sync messages.", true))
                }
            }
        }
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
}