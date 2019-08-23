package com.potados.geomms.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.potados.geomms.usecase.SendUpdate
import org.koin.core.KoinComponent
import org.koin.core.inject

/**
 * Manifest registered.
 * Receive explicit intent only.
 *
 * Invoke [SendUpdate].
 *
 * @see [SendUpdate]
 */
class SendUpdateReceiver : BroadcastReceiver(), KoinComponent {
    private val sendUpdate: SendUpdate by inject()

    override fun onReceive(context: Context, intent: Intent) {
        val connectionId = intent.getLongExtra(EXTRA_CONNECTION_ID, 0L)

        sendUpdate(connectionId)
    }

    companion object {
        const val EXTRA_CONNECTION_ID = "connectionId"
    }
}