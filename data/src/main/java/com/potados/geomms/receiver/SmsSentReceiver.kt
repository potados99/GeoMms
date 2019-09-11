
package com.potados.geomms.receiver

import android.app.Activity
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.telephony.SmsManager
import com.potados.geomms.receiver.SmsSentReceiver.Companion.ACTION
import com.potados.geomms.usecase.MarkFailed
import com.potados.geomms.usecase.MarkSent
import org.koin.core.KoinComponent
import org.koin.core.inject

/**
 * Not registered in manifest.
 * Receive both explicit intent and
 * implicit intent with action of [ACTION].
 *
 * Handle the result of sending SMS and invoke
 * [MarkSent] or [MarkFailed].
 *
 * @see [MarkSent]
 * @see [MarkFailed]
 */
class SmsSentReceiver : BroadcastReceiver(), KoinComponent {

    companion object {
        /**
         * [MmsSentReceiver] receives implicit intent.
         * So it does also.
         *
         * There could be another broadcast receiver that receives
         * intent with [ACTION].
         */
        const val ACTION = "com.potados.geomms.SMS_SENT"
    }

    private val markSent: MarkSent by inject()
    private val markFailed: MarkFailed by inject()

    override fun onReceive(context: Context, intent: Intent) {
        // This receiver is instantiated registered when sms is sent.
        // After it has done its duty, unregister it.
        context.unregisterReceiver(this)

        val id = intent.getLongExtra("id", 0L)

        when (resultCode) {
            Activity.RESULT_OK -> {
                val pendingResult = goAsync()
                markSent(id) { pendingResult.finish() }
            }

            SmsManager.RESULT_ERROR_GENERIC_FAILURE,
            SmsManager.RESULT_ERROR_NO_SERVICE,
            SmsManager.RESULT_ERROR_NULL_PDU,
            SmsManager.RESULT_ERROR_RADIO_OFF -> {
                val pendingResult = goAsync()
                markFailed(MarkFailed.Params(id, resultCode)) { pendingResult.finish() }
            }
        }
    }
}