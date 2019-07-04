package com.potados.geomms.data

import androidx.lifecycle.LiveData

interface MessageRepository {
    fun updateConversationList() /* re-query and update */

    fun getConversationHeads(): List<ShortMessage>

    fun getLiveConversationHeads(): LiveData<List<ShortMessage>>

    fun getSmsThreadByThreadId(threadId: Long): SmsThread

    fun addSms(sms: ShortMessage)

    fun deleteSms(id: Int)
}