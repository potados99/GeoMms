package com.potados.geomms.feature.conversations

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.potados.geomms.common.base.BaseViewModel
import com.potados.geomms.model.Conversation
import com.potados.geomms.repository.ConversationRepository
import com.potados.geomms.repository.SyncRepository
import io.realm.RealmResults
import org.koin.core.KoinComponent
import org.koin.core.inject

/**
 * ConversationListFragment를 보조할 뷰모델입니다.
 * 대화방들의 정보를 가지고 있습니다.
 */
class ConversationsViewModel : ViewModel(), KoinComponent {

    private val conversationRepo: ConversationRepository by inject()

    val conversations = conversationRepo.getConversations()

    // Permission hint snackbar
    var defaultSmsState: Boolean = false

    // Sync progress
    var syncState: SyncRepository.SyncProgress = SyncRepository.SyncProgress.Idle()
}