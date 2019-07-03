package com.potados.geomms.data

import androidx.lifecycle.LiveData

interface MessageRepository {
    fun updateRepository()

    fun getConversationHeads(): List<Sms>

    fun getLiveConversationHeads(): LiveData<List<Sms>>

    fun getSmsThreadByThreadId(threadId: Int): SmsThread?

    fun addSms(sms: Sms)

    fun deleteSms(id: Int)
}