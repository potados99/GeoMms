package com.potados.geomms.feature.compose

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.potados.geomms.manager.ActiveConversationManager
import com.potados.geomms.model.Conversation
import com.potados.geomms.model.Message
import com.potados.geomms.repository.ConversationRepository
import com.potados.geomms.repository.MessageRepository
import com.potados.geomms.usecase.MarkRead
import com.potados.geomms.usecase.SendMessage
import io.realm.Realm
import io.realm.RealmResults
import org.koin.core.KoinComponent
import org.koin.core.inject
import timber.log.Timber
import kotlin.concurrent.thread

/**
 * ConversationActivity를 보조할 뷰모델입니다.
 */
class ComposeViewModel : ViewModel(), KoinComponent {

    /***********************************************************
     * UseCase
     ***********************************************************/
    private val sendMessage: SendMessage by inject()
    private val markRead: MarkRead by inject()

    private val activeConversationManager: ActiveConversationManager by inject()
    private val conversationRepo: ConversationRepository by inject()
    private val messageRepo: MessageRepository by inject()

    lateinit var conversation: Conversation
    lateinit var messages: RealmResults<Message>

    fun start(threadId: Long) {
        conversation = conversationRepo.getConversation(threadId)!!
        messages = messageRepo.getMessages(threadId)
        activeConversationManager.setActiveConversation(threadId)

        markRead(listOf(threadId))

        Timber.i("viewmodel started.")
    }

    /* 임시 TODO */
    fun sendSms(body: String) {
        sendMessage(
            SendMessage.Params(
                subId = -1,
                threadId = conversation.id,
                addresses = conversation.recipients.map { it.address },
                body = body,
                attachments = listOf()
        ))
    }

    /**
     * 리사이클러뷰가 최하단에 도달했는지 여부.
     * 초기값은 참.
     */
    var recyclerViewReachedItsEnd: Boolean = true
}

