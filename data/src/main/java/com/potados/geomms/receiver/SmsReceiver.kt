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

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.provider.Telephony.Sms
import com.potados.geomms.service.LocationSupportService
import com.potados.geomms.usecase.ReceivePacket
import com.potados.geomms.usecase.ReceiveSms
import org.koin.core.KoinComponent
import org.koin.core.inject
import timber.log.Timber

/**
 * Manifest registered.
 * Receive both explicit intent and
 * implicit intent with action of [android.provider.Telephony.SMS_DELIVER].
 *
 * Handle incoming SMS, and invoke [ReceiveSms] or [ReceivePacket]
 *
 * If the SMS body starts with Location Support prefix,
 * [ReceivePacket] takes it, or [ReceiveSms] does.
 *
 * @see [ReceiveSms]
 * @see [ReceivePacket]
 * @see [LocationSupportService]
 */
class SmsReceiver : BroadcastReceiver(), KoinComponent {

    private val receiveMessage: ReceiveSms by inject()
    private val receivePacket: ReceivePacket by inject()
    private val locationService: LocationSupportService by inject()

    override fun onReceive(context: Context, intent: Intent) {
        Timber.v("onReceive")

        Sms.Intents.getMessagesFromIntent(intent)?.let { messages ->
            val pendingResult = goAsync()

            val subId = intent.extras?.getInt("subscription", -1) ?: -1
            val isPacket = messages.isNotEmpty() &&
                    locationService.isValidPacket(
                        messages
                            .mapNotNull { it.displayMessageBody }
                            .reduce { body, new -> body + new }
                    )

            when (isPacket) {
                true -> receivePacket(messages) {
                    pendingResult.finish()
                    Timber.i("received packet.")
                }
                else -> receiveMessage(ReceiveSms.Params(subId, messages)) {
                    pendingResult.finish()
                    Timber.i("received SMS.")
                }
            }
        }
    }
}