package com.potados.geomms.feature.message

import androidx.lifecycle.MutableLiveData
import com.potados.geomms.common.base.BaseViewModel
import com.potados.geomms.model.Conversation
import org.koin.core.KoinComponent
import org.koin.core.inject

/**
 * ConversationActivity를 보조할 뷰모델입니다.
 */
class ComposeViewModel : BaseViewModel(), KoinComponent {

    /***********************************************************
     * UseCase
     ***********************************************************/

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

