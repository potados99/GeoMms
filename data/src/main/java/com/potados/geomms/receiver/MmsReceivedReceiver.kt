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

import android.content.ContentResolver
import android.content.ContentUris
import android.net.Uri
import android.provider.Telephony
import com.klinker.android.send_message.MmsReceivedReceiver
import com.potados.geomms.extension.insertOrUpdate
import com.potados.geomms.extension.tryOrNull
import com.potados.geomms.mapper.CursorToMessage
import com.potados.geomms.service.LocationSupportService
import com.potados.geomms.usecase.ReceiveMMSPacket
import com.potados.geomms.usecase.ReceiveMms
import com.potados.geomms.usecase.ReceivePacket
import org.koin.core.KoinComponent
import org.koin.core.inject
import timber.log.Timber
import kotlin.system.exitProcess

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

    private val service: LocationSupportService by inject()
    private val receiveMms: ReceiveMms by inject()
    private val receiveMMSPacket: ReceiveMMSPacket by inject()
    private val contentResolver: ContentResolver by inject()
    private val cursorToMessage: CursorToMessage by inject()

    override fun onMessageReceived(messageUri: Uri?) {
        Timber.v("onMessageReceived")

        messageUri?.let { uri ->
            val pendingResult = goAsync()

            // Let's see if this MMS is a geo-mms packet.
            val id = ContentUris.parseId(uri)
            val stableUri = ContentUris.withAppendedId(Telephony.Mms.CONTENT_URI, id)
            val message = contentResolver.query(stableUri, null, null, null, null)?.use {
                val columnsMap = CursorToMessage.MessageColumns(it)
                cursorToMessage.map(Pair(it, columnsMap))
            }

            // If then, use receiveMMSPacket rather than receiveMms.
            if (message?.body != null && service.isValidPacket(message.body)) {
                // At this point the MMS is already stored in the content provider.
                // So we need to delete it.
                contentResolver.delete(stableUri, null, null)
                receiveMMSPacket(message) {
                    pendingResult.finish()
                    Timber.i("received MMS location packet.")
                }
            }
            else {
                receiveMms(uri) {
                    pendingResult.finish()
                    Timber.i("received MMS.")
                }
            }
        }
    }

}
