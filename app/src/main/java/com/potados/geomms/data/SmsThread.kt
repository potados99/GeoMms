package com.potados.geomms.data

class SmsThread(private val messages: List<ShortMessage>) {



    fun peekLatest(): ShortMessage? = messages.last()
}