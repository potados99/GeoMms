package com.potados.geomms.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.potados.geomms.data.MessageRepository
import com.potados.geomms.data.ShortMessage
import org.koin.core.KoinComponent
import org.koin.core.inject

class MessageListViewModel : ViewModel(), KoinComponent {
    private val messageRepo: MessageRepository by inject()

    /**
     * 대화방 목록입니다. 각 대화방의 마지막(최근) 메시지가 그 대화방을 대표합니다.
     */
    fun getConversationHeads(): LiveData<List<ShortMessage>> = messageRepo.getLiveConversationHeads()
    fun addConversationHead(head: ShortMessage) {
        if (head.threadId in messageRepo.getConversationHeads().map { it.threadId }) {
            /**
             * 이미 있는 대화방인 경우.
             */
        }
        // messageRepo.addSms(head)
    }
    fun updateConversationHeads() {
        messageRepo.updateConversationList()
    }
}