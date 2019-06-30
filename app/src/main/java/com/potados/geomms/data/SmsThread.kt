package com.potados.geomms.data

import android.provider.Telephony

class SmsThread(private val messages: List<Sms>) {



    fun peekLatest(): Sms? = messages.last()
}