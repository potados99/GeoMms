/*
 * Copyright (C) 2019 Song Byeong Jun <potados99@gmail.com>
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
