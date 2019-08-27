/*
 * Copyright (C) 2017 Moez Bhatti <moez.bhatti@gmail.com>
 *
 * This file is part of QKSMS.
 *
 * QKSMS is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * QKSMS is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with QKSMS.  If not, see <http://www.gnu.org/licenses/>.
 */
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
import timber.log.Timber

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
        context.unregisterReceiver(this) /* 일회용 */

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