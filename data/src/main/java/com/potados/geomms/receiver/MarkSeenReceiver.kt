package com.potados.geomms.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.potados.geomms.usecase.MarkSeen
import org.koin.core.KoinComponent
import org.koin.core.inject

/**
 * Manifest registered.
 * Receive explicit intent only.
 *
 * Invoke [MarkSeen].
 *
 * 'seen' means the conversation is exposed to user but not read,
 * like when user saw a new message by notification preview and ignoring it.
 *
 * @see [MarkSeen]
 */
class MarkSeenReceiver : BroadcastReceiver(), KoinComponent {

    private val markSeen: MarkSeen by inject()

    override fun onReceive(context: Context, intent: Intent) {
        val pendingResult = goAsync()
        val threadId = intent.getLongExtra("threadId", 0)
        markSeen(threadId) { pendingResult.finish() }
    }

}