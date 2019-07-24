package com.potados.geomms.feature.viewmodel

import androidx.lifecycle.MutableLiveData
import com.potados.geomms.core.exception.Failure
import com.potados.geomms.core.functional.Either
import com.potados.geomms.core.interactor.UseCase
import com.potados.geomms.core.platform.BaseViewModel
import com.potados.geomms.feature.data.entity.SmsThread
import com.potados.geomms.feature.usecase.GetConversations
import com.potados.geomms.feature.usecase.ReadConversation
import org.koin.core.KoinComponent
import org.koin.core.inject

/**
 * ConversationListFragment를 보조할 뷰모델입니다.
 * 대화방들의 정보를 가지고 있습니다.
 */
class ConversationListViewModel : BaseViewModel(), KoinComponent {

    /***********************************************************
     * UseCase
     ***********************************************************/
    private val getConversations: GetConversations by inject()
    private val readConversation: ReadConversation by inject()


    val conversations = MutableLiveData<List<SmsThread>>()

    fun loadConversations() = getConversations(UseCase.None()) {
        it.either(::handleFailure, ::handleConversationList)
    }

    private fun handleConversationList(conversations: List<SmsThread>) {
        this.conversations.value = conversations
    }

    fun setAsRead(conversation: SmsThread) = readConversation(conversation) {
            it.either(::handleFailure) { /* 성공하면 할게 없다. */ }
        }
}