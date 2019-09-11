package com.potados.geomms.common.extension

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent


fun newBroadcastReceiver(onReceive: (intent: Intent?) -> Unit): BroadcastReceiver {
    return object: BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            onReceive(intent)
        }
    }
}