package com.potados.geomms.feature.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.potados.geomms.core.platform.BaseViewModel
import com.potados.geomms.feature.data.repository.ContactRepository
import com.potados.geomms.feature.data.repository.MessageRepository
import com.potados.geomms.feature.data.entity.ShortMessage
import com.potados.geomms.feature.data.entity.SmsThread
import com.potados.geomms.feature.usecases.GetMessages
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


    /**
     * 연락처 가져올 때에 사용.
     */
    private val contactRepo: ContactRepository by inject()

    val messages = MutableLiveData<List<ShortMessage>>()

    fun loadMessages(thread: SmsThread) = getMessages(thread) {
        it.either(::handleFailure, ::handleMessages)
    }

    private fun handleMessages(messages: List<ShortMessage>) {
        this.messages.value = messages
    }

    fun getRecipients(thread: SmsThread): String = thread.getRecipientString(contactRepo)

    /**
     * 리사이클러뷰가 최하단에 도달했는지 여부.
     * 초기값은 참.
     */
    var recyclerViewReachedItsEnd: Boolean = true

}