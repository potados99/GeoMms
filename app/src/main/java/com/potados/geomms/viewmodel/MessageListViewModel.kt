package com.potados.geomms.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.potados.geomms.data.MessageRepository
import com.potados.geomms.data.ShortMessage
import com.potados.geomms.data.SmsThread
import org.koin.core.KoinComponent
import org.koin.core.inject

class MessageListViewModel : ViewModel(), KoinComponent {
    val messageRepo: MessageRepository by inject()

    private val conversations: MutableList<SmsThread> = mutableListOf()
    private val liveDataOfConversations = MutableLiveData<List<SmsThread>>()

    init {
        updateConversations()
    }

    fun getConversations(): LiveData<List<SmsThread>> = liveDataOfConversations
    fun updateConversations() {
        conversations.clear()
        conversations.addAll(messageRepo.getSmsThreads())
        liveDataOfConversations.value = conversations
    }
}