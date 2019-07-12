package com.potados.geomms.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.provider.Telephony
import android.telephony.SmsMessage
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.potados.geomms.protocol.LocationSupportProtocol
import java.util.*

/**
 * SMS를 수신했을 때에 작동합니다.
 */
class SmsReceiver : BroadcastReceiver() {

    private val messageQueue = PriorityQueue<SmsMessage>()
    private val liveMessageQueue = MutableLiveData<PriorityQueue<SmsMessage>>()

    fun getMessageQueue(): LiveData<PriorityQueue<SmsMessage>> {
        return liveMessageQueue
    }

    override fun onReceive(context: Context?, intent: Intent?) {
        if (context == null) return
        if (intent == null) return

        if (intent.action == Telephony.Sms.Intents.SMS_DELIVER_ACTION) {
            messageQueue.clear()
            messageQueue.addAll(
                Telephony.Sms.Intents.getMessagesFromIntent(intent)
            )

            liveMessageQueue.value = messageQueue
        }
    }
}