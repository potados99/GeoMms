package com.potados.geomms.app

import android.app.Activity
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log

class SmsSentReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        if (context == null) return
        if (intent == null) return

        when (resultCode) {
            Activity.RESULT_OK -> {
                Log.d("SmsSentReceiver:onReceive", "SMS send success.")

                /**
                 * OKAY
                 */
            }
            else -> {
                Log.d("SmsSentReceiver:onReceive", "SMS send failed.")
            }
        }
    }

    companion object {
        const val SMS_SENT = "com.potados.SMS_SENT"
    }
}