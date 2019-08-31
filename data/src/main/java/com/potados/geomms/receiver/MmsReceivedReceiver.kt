package com.potados.geomms.receiver

import android.net.Uri
import com.klinker.android.send_message.MmsReceivedReceiver
import com.potados.geomms.usecase.ReceiveMms
import org.koin.core.KoinComponent
import org.koin.core.inject
import timber.log.Timber

/**
 * Manifest registered.
 * Receive both explicit intent and
 * implicit intent with action of [com.klinker.android.messaging.MMS_RECEIVED].
 *
 * After MMS handled by [MmsReceiver], invoke [ReceiveMms].
 *
 * @see [MmsReceiver]
 * @see [ReceiveMms]
 */
class MmsReceivedReceiver : MmsReceivedReceiver(), KoinComponent {

    private val receiveMms: ReceiveMms by inject()

    override fun onMessageReceived(messageUri: Uri?) {
        Timber.v("onMessageReceived")

        messageUri?.let { uri ->
            val pendingResult = goAsync()
            receiveMms(uri) {
                pendingResult.finish()
                Timber.i("received MMS.")
            }
        }
    }

}
