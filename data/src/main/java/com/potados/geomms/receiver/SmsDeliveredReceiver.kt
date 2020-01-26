/*
 * Copyright (C) 2019-2020 Song Byeong Jun <potados99@gmail.com>
 *
 * This file is part of GeoMms.
 *
 * GeoMms is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * GeoMms is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with GeoMms.  If not, see <http://www.gnu.org/licenses/>.
 */

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