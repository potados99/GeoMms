package com.potados.geomms.feature.conversations

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.potados.geomms.common.base.BaseViewModel
import com.potados.geomms.manager.PermissionManager
import com.potados.geomms.model.Conversation
import com.potados.geomms.model.SyncLog
import com.potados.geomms.repository.ConversationRepository
import com.potados.geomms.repository.SyncRepository
import com.potados.geomms.usecase.SyncMessages
import io.realm.Realm
import io.realm.RealmResults
import org.koin.core.KoinComponent
import org.koin.core.inject

/**
 * ConversationListFragment를 보조할 뷰모델입니다.
 * 대화방들의 정보를 가지고 있습니다.
 */
class ConversationsViewModel : ViewModel(), KoinComponent {

    private val conversationRepo: ConversationRepository by inject()
    private val syncRepo: SyncRepository by inject()
    private val permissionManager: PermissionManager by inject()

    init {
        // If we have all permissions and we've never run a sync, run a sync. This will be the case
        // when upgrading from 2.7.3, or if the app's data was cleared
        val lastSync = Realm.getDefaultInstance().use { realm -> realm.where(SyncLog::class.java)?.max("date") ?: 0 }
        if (lastSync == 0 && permissionManager.isDefaultSms() && permissionManager.hasReadSms() && permissionManager.hasContacts()) {
            syncRepo.syncMessages()
        }
    }

    val conversations = conversationRepo.getConversations()

    // Permission hint snackbar
    val defaultSmsState = permissionManager.isDefaultSmsLiveData()

    // Sync progress
    val syncState = syncRepo.syncProgress
}