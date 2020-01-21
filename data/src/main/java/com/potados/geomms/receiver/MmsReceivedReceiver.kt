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
import com.potados.geomms.model.Message
import com.potados.geomms.service.LocationSupportService
import com.potados.geomms.usecase.ReceiveMMSPacket
import com.potados.geomms.usecase.ReceiveMms
import com.potados.geomms.usecase.ReceivePacket
import com.potados.geomms.usecase.SyncMessage
import io.realm.Realm
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

    private fun getExistingId(uri: Uri): Long? {
        val type = when {
            uri.toString().contains("mms") -> "mms"
            uri.toString().contains("sms") -> "sms"
            else -> throw RuntimeException("Wrong!!!!!")
        }

        // If we don't have a valid id, return null
        val id = tryOrNull(false) { ContentUris.parseId(uri) }
            ?: throw RuntimeException("Failed to sync message. No valid id for given uri.")

        // Check if the message already exists, so we can reuse the id
        return Realm.getDefaultInstance().use { realm ->
            realm.refresh()
            realm.where(Message::class.java)
                .equalTo("type", type)
                .equalTo("contentId", id)
                .findFirst()
                ?.id
        }
    }

    private fun getStableUri(uri: Uri): Uri {
        val type = when {
            uri.toString().contains("mms") -> "mms"
            uri.toString().contains("sms") -> "sms"
            else -> throw RuntimeException("Wrong!!!!!")
        }

        // If we don't have a valid id, return null
        val id = tryOrNull(false) { ContentUris.parseId(uri) }
            ?: throw RuntimeException("Failed to sync message. No valid id for given uri.")

        // The uri might be something like content://mms/inbox/id
        // The box might change though, so we should just use the mms/id uri
        return when (type) {
            "mms" -> ContentUris.withAppendedId(Telephony.Mms.CONTENT_URI, id)
            else -> ContentUris.withAppendedId(Telephony.Sms.CONTENT_URI, id)
        }
    }

    private fun previewMessage(uri: Uri): Message? {
        val existingId = getExistingId(uri)
        val stableUri = getStableUri(uri)

        return contentResolver.query(stableUri, null, null, null, null)?.use { cursor ->
            // If there are no rows, return null. Otherwise, we've moved to the first row
            if (!cursor.moveToFirst()) return null

            val columnsMap = CursorToMessage.MessageColumns(cursor)
            cursorToMessage.map(Pair(cursor, columnsMap)).apply {
                existingId?.let { this.id = it }
            }
        }
    }

    private fun getBody(message: Message): String? {
        if (message.isSms()) {
            return message.body
        } else {
            return message.parts
                .filter { it.type == "text/plain" }
                .map { it.text }
                .takeIf { it.isNotEmpty() }
                ?.reduce { acc, part -> acc + part }
        }
    }

    override fun onMessageReceived(messageUri: Uri?) {
        Timber.v("onMessageReceived")

        messageUri?.let { uri ->
            val pendingResult = goAsync()

            // Let's see if this MMS is a geo-mms packet.
            val stableUri = getStableUri(uri)
            val message = previewMessage(uri) ?: return
            val body = getBody(message)
            val address = message.address

            // If then, use receiveMMSPacket rather than receiveMms.
            if (body != null && service.isValidPacket(body)) {
                receiveMMSPacket(Pair(address, body)) {
                    pendingResult.finish()
                    Timber.i("received MMS location packet.")
                }

                // At this point the MMS is already stored in the content provider.
                // So we need to delete it.
                contentResolver.delete(stableUri, null, null)
            }
            else {
                Timber.i("Time to receive message!")
                receiveMms(uri) {
                    pendingResult.finish()
                    Timber.i("received MMS.")
                }
            }
        }
    }

}
