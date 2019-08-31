package com.potados.geomms.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.potados.geomms.usecase.MarkRead
import org.koin.core.KoinComponent
import org.koin.core.inject

/**
 * Manifest registered.
 * Receive explicit intent only.
 *
 * Invoke [MarkRead].
 *
 * @see [MarkRead]
 */
class MarkReadReceiver : BroadcastReceiver(), KoinComponent {

    private val markRead: MarkRead by inject()

    override fun onReceive(context: Context, intent: Intent) {
        val pendingResult = goAsync()
        val threadId = intent.getLongExtra("threadId", 0)
        markRead(listOf(threadId)) { pendingResult.finish() }
    }

}