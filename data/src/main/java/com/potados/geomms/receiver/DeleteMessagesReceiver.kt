
package com.potados.geomms.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.potados.geomms.usecase.DeleteMessages
import org.koin.core.KoinComponent
import org.koin.core.inject

/**
 * Manifest registered.
 * Receive explicit intent only.
 *
 * Invoke [DeleteMessages].
 *
 * @see [DeleteMessages]
 */
class DeleteMessagesReceiver : BroadcastReceiver(), KoinComponent {

    private val deleteMessages: DeleteMessages by inject()

    override fun onReceive(context: Context, intent: Intent) {
        val pendingResult = goAsync()
        val threadId = intent.getLongExtra("threadId", 0)
        val messageIds = intent.getLongArrayExtra("messageIds")

        deleteMessages(DeleteMessages.Params(messageIds.toList(), threadId)) { pendingResult.finish() }
    }

}