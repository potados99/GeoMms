package com.potados.geomms.common.base.interfaces

import android.content.IntentFilter

interface HasSmsReceiver {
    fun intentFilter(): IntentFilter?
    fun smsReceivedBehavior(): ((address: String, body: String, date: Long) -> Any?)?
}