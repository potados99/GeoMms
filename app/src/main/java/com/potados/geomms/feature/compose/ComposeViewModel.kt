package com.potados.geomms.feature.compose

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.potados.geomms.common.base.BaseViewModel
import com.potados.geomms.model.Conversation
import com.potados.geomms.model.Message
import com.potados.geomms.repository.ConversationRepository
import com.potados.geomms.repository.MessageRepository
import com.potados.geomms.usecase.SendMessage
import io.realm.Realm
import io.realm.RealmResults
import org.koin.core.KoinComponent
import org.koin.core.inject

/**
 * ConversationActivity를 보조할 뷰모델입니다.
 */
class ComposeViewModel : ViewModel(), KoinComponent {

    /***********************************************************
     * UseCase
     ***********************************************************/
    private val sendMessage: SendMessage by inject()

    private val conversationRepo: ConversationRepository by inject()
    private val messageRepo: MessageRepository by inject()

    lateinit var conversation: Conversation

    lateinit var messages: RealmResults<Message>

    fun start(threadId: Long) {
        conversation = conversationRepo.getConversation(threadId)!!
        messages = messageRepo.getMessages(threadId)
    }

    /**
     * 리사이클러뷰가 최하단에 도달했는지 여부.
     * 초기값은 참.
     */
    var recyclerViewReachedItsEnd: Boolean = true
}

