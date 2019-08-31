package com.potados.geomms.receiver

import android.app.Activity
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.potados.geomms.receiver.SmsDeliveredReceiver.Companion.ACTION
import com.potados.geomms.receiver.SmsSentReceiver.Companion.ACTION
import com.potados.geomms.usecase.MarkDelivered
import com.potados.geomms.usecase.MarkDeliveryFailed
import org.koin.core.KoinComponent
import org.koin.core.inject

/**
 * Not registered in manifest.
 * Receive both explicit intent and
 * implicit intent with action of [ACTION].
 *
 * Handle the result of SMS delivery and invoke
 * [MarkDelivered] or [markDeliveryFailed].
 *
 * @see [MarkDelivered]
 * @see [MarkDeliveryFailed]
 */
class SmsDeliveredReceiver : BroadcastReceiver(), KoinComponent {

    companion object {
        /**
         * [MmsUpdatedReceiver] receives implicit intent.
         * So it does also.
         *
         * There could be another broadcast receiver that receives
         * intent with [ACTION].
         */
        const val ACTION = "com.potados.geomms.SMS_DELIVERED"
    }

    private val markDelivered: MarkDelivered by inject()
    private val markDeliveryFailed: MarkDeliveryFailed by inject()

    override fun onReceive(context: Context, intent: Intent) {
        // This receiver is instantiated registered when sms is sent.
        // After it has done its duty, unregister it.
        context.unregisterReceiver(this)

        val id = intent.getLongExtra("id", 0L)

        when (resultCode) {
            // TODO notify about delivery
            Activity.RESULT_OK -> {
                val pendingResult = goAsync()
                markDelivered(id) { pendingResult.finish() }
            }

            // TODO notify about delivery failure
            Activity.RESULT_CANCELED -> {
                val pendingResult = goAsync()
                markDeliveryFailed(MarkDeliveryFailed.Params(id, resultCode)) { pendingResult.finish() }
            }
        }
    }

}