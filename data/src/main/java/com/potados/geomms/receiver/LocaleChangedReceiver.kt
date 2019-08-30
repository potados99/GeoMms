package com.potados.geomms.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.potados.geomms.usecase.SyncContacts
import org.koin.core.KoinComponent
import org.koin.core.inject

/**
 * Manifest registered.
 * Receive both explicit intent and
 * implicit intent with action of [android.provider.action.DEFAULT_SMS_PACKAGE_CHANGED].
 *
 * Invoke [SyncContacts].
 *
 * @see [SyncContacts]
 */
class LocaleChangedReceiver : BroadcastReceiver(), KoinComponent {

    private val syncContacts: SyncContacts by inject()

    override fun onReceive(context: Context?, intent: Intent?) {
        val pendingResult = goAsync()
        syncContacts(Unit) { pendingResult.finish() }
    }
}