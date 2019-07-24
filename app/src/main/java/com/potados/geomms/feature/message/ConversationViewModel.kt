package com.potados.geomms.feature.message

import androidx.lifecycle.MutableLiveData
import com.potados.geomms.core.platform.BaseViewModel
import com.potados.geomms.feature.common.ContactRepository
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

    lateinit var thread: SmsThread

    /**
     * 연락처 가져올 때에 사용.
     */
    private val contactRepo: ContactRepository by inject()

    val messages = MutableLiveData<List<ShortMessage>>()

    fun loadMessages() = getMessages(thread) {
        it.either(::handleFailure, ::handleMessages)
    }

    private fun handleMessages(messages: List<ShortMessage>) {
        this.messages.value = messages
    }

    fun sendMessage(body: String) =
        sendSms(SmsEntity().address(firstRecipient()).body(body)) {
            it.either(::handleFailure) {
                loadMessages()
            }
        }

    fun setAsRead() = readConversation(thread) {
        it.either(::handleFailure) { /* 성공하면 할게 없다. */ }
    }

    fun recipients(): String = thread.getRecipientString(contactRepo)

    fun firstRecipient(): String = recipients().split(',')[0].trim(' ')

    /**
     * 리사이클러뷰가 최하단에 도달했는지 여부.
     * 초기값은 참.
     */
    var recyclerViewReachedItsEnd: Boolean = true

}