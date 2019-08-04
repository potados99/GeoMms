package com.potados.geomms.feature.message.representation

import androidx.lifecycle.MutableLiveData
import com.potados.geomms.core.platform.BaseViewModel
import com.potados.geomms.feature.message.domain.usecase.SendSms
import com.potados.geomms.feature.message.domain.Conversation
import com.potados.geomms.feature.message.domain.Sms
import com.potados.geomms.feature.message.domain.SmsComposed
import com.potados.geomms.feature.message.domain.usecase.GetConversation
import com.potados.geomms.feature.message.domain.usecase.GetMessages
import com.potados.geomms.feature.message.domain.usecase.ReadConversation
import org.koin.core.KoinComponent
import org.koin.core.inject

/**
 * ConversationActivity를 보조할 뷰모델입니다.
 */
class ConversationViewModel : BaseViewModel(), KoinComponent {

    /***********************************************************
     * UseCase
     ***********************************************************/
    private val getConversation: GetConversation by inject()
    private val getMessages: GetMessages by inject()
    private val sendSms: SendSms by inject()
    private val readConversation: ReadConversation by inject()

    val conversation = MutableLiveData<Conversation>()

    val messages = MutableLiveData<List<Sms>>()

    fun start(id: Long) {
        setConversationId(id)
    }

    private fun setConversationId(id: Long) {
        getConversation(id) {
            it.either( {
                conversation.value = it
                loadMessages()
            }, ::handleFailure)
        }
    }

    fun loadMessages() = conversation.value?.let {
        getMessages(it) { result ->
            result.onSuccess { data ->
                messages.value = data
            }.onError(::handleFailure)
        }
    }

    fun setAsRead() = conversation.value?.let {
        readConversation(it) { result ->
            result.onError(::handleFailure)
        }
    }

    fun sendMessage(body: String) = conversation.value?.let {
        it.recipients.forEach { person ->
            sendSms(SmsComposed(person.phoneNumber, body)) { result ->
                result.onError(::handleFailure)
            }
        }
    }

    /**
     * 리사이클러뷰가 최하단에 도달했는지 여부.
     * 초기값은 참.
     */
    var recyclerViewReachedItsEnd: Boolean = true
}

