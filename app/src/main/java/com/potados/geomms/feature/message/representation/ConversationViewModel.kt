package com.potados.geomms.feature.message.representation

import androidx.lifecycle.MutableLiveData
import com.potados.geomms.core.platform.BaseViewModel
import com.potados.geomms.feature.common.ContactRepository
import com.potados.geomms.feature.message.domain.usecase.SendSms
import com.potados.geomms.feature.message.data.SmsEntity
import com.potados.geomms.feature.message.data.ConversationEntity
import com.potados.geomms.feature.message.domain.Conversation
import com.potados.geomms.feature.message.domain.Sms
import com.potados.geomms.feature.message.domain.SmsComposed
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
    private val getMessages: GetMessages by inject()
    private val sendSms: SendSms by inject()
    private val readConversation: ReadConversation by inject()

    lateinit var thread: Conversation

    /**
     * 연락처 가져올 때에 사용.
     */
    private val contactRepo: ContactRepository by inject()

    val messages = MutableLiveData<List<Sms>>()

    fun loadMessages() = getMessages(thread) { result ->
        result.onSuccess { data ->
            messages.value = data
        }.onError(::handleFailure)
    }

    fun sendMessage(body: String) =
        thread.recipients.forEach {
            sendSms(SmsComposed(it.phoneNumber, body)) { result ->
                result.onError(::handleFailure)
            }
        }


    fun setAsRead() = readConversation(thread) { result ->
        result.onError(::handleFailure)
    }

    fun contactNames() = thread.recipients.map { it.contactName }

    /**
     * 리사이클러뷰가 최하단에 도달했는지 여부.
     * 초기값은 참.
     */
    var recyclerViewReachedItsEnd: Boolean = true

}