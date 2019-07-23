package com.potados.geomms.feature.receiver

import android.app.Activity
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.potados.geomms.core.util.Notify

class SmsSentReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        if (context == null) return
        if (intent == null) return

        when (resultCode) {
            Activity.RESULT_OK -> {
                /**
                 * OKAY
                 */
            }
            else -> {
                Notify(context).short("SMS sent failed.")
            }
        }
    }
}