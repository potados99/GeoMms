
package com.potados.geomms.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.net.Uri
import com.potados.geomms.usecase.SyncMessage
import org.koin.core.KoinComponent
import org.koin.core.inject

/**
 * Manifest registered.
 * Receive both explicit intent and
 * implicit intent with action of [com.potados.geomms.MMS_UPDATED].
 *
 * Invoke [SyncMessage] with given uri of the MMS.
 *
 * @see [SyncMessage]
 */
class MmsUpdatedReceiver : BroadcastReceiver(), KoinComponent {

    companion object {
        const val URI = "uri"
    }

    private val syncMessage: SyncMessage by inject()

    override fun onReceive(context: Context, intent: Intent) {
        intent.getStringExtra(URI)?.let { uriString ->
            val pendingResult = goAsync()
            syncMessage(Uri.parse(uriString)) { pendingResult.finish() }
        }
    }

}