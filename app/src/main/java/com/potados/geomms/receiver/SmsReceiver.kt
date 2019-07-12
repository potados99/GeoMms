package com.potados.geomms.receiver

import android.content.BroadcastReceiver
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Telephony
import android.telephony.SmsMessage
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.potados.geomms.protocol.LocationSupportProtocol
import com.potados.geomms.util.Notify
import java.util.*

/**
 * SMS를 수신했을 때에 작동합니다.
 */
class SmsReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context?, intent: Intent?) {
        if (context == null) return
        if (intent == null) return

        if (intent.action == Telephony.Sms.Intents.SMS_DELIVER_ACTION) {
            Telephony.Sms.Intents.getMessagesFromIntent(intent).forEach {
                processMessage(context, it)
            }
        }
    }

    private fun processMessage(context: Context, message: SmsMessage) {
        if (LocationSupportProtocol.isLocationSupportMessage(message.messageBody)) {
            Notify(context).short("Open Geo MMS!")
        }
        else {
            val address = message.originatingAddress
            val body = message.messageBody

            // add message to inbox.
            val values = ContentValues()

            values.put("address", address)
            values.put("body", body)
            /* 나머지 column은 자동으로 채워짐! */

            context.contentResolver.insert(Uri.parse("content://sms/inbox"), values)
        }

        val intent = Intent().apply {
            action = "com.potados.geomms.SMS_DELIVER"
            putExtra("address", message.originatingAddress)
            putExtra("body", message.messageBody)
            putExtra("date", message.timestampMillis)
        }

        context.sendBroadcast(intent)
    }
}