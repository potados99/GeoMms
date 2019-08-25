package com.potados.geomms.feature.conversations

import androidx.lifecycle.ViewModel
import com.potados.geomms.common.base.BaseViewModel
import com.potados.geomms.manager.PermissionManager
import com.potados.geomms.model.SyncLog
import com.potados.geomms.repository.ConversationRepository
import com.potados.geomms.repository.SyncRepository
import com.potados.geomms.usecase.SyncMessages
import io.realm.Realm
import org.koin.core.KoinComponent
import org.koin.core.inject

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
     */
    val conversations = conversationRepo.getConversations()

    /**
     * Binding element.
     * Target: [main_hint.xml]
     */
    val defaultSmsState = permissionManager.isDefaultSmsLiveData()

    /**
     * Binding element
     * Target: [main_syncing.xml]
     */
    val syncState = syncRepo.syncProgress

    override fun start() {
        super.start()

        failables.addAll(
            listOf(
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
            syncMessages(Unit)
        }
    }
}