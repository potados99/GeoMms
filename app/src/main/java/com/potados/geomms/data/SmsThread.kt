package com.potados.geomms.data

class SmsThread(private val messages: List<ShortMessage>) {

    fun allMessages(): List<ShortMessage> = messages

    fun peekLatest(): ShortMessage? = messages.last()

    companion object {

    }
}