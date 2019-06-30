package com.potados.geomms.data

interface MessageRepository {
    fun updateRepository()

    fun getConversationHeads(): List<Sms>

    fun getSmsThreadByThreadId(threadId: Int): SmsThread?

    fun addSms(sms: Sms)

    fun deleteSms(id: Int)
}