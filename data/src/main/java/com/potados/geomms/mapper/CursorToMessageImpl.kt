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

 package com.potados.geomms.mapper

import android.content.Context
import android.database.Cursor
import android.provider.Telephony.*
import androidx.core.net.toUri
import com.google.android.mms.pdu_alt.EncodedStringValue
import com.google.android.mms.pdu_alt.PduHeaders
import com.google.android.mms.pdu_alt.PduPersister
import com.potados.geomms.extension.map
import com.potados.geomms.manager.KeyManager
import com.potados.geomms.manager.KeyManagerImpl.Companion.CHANNEL_MESSAGE
import com.potados.geomms.manager.PermissionManager
import com.potados.geomms.model.Message
import com.potados.geomms.util.SqliteWrapper
import timber.log.Timber

class CursorToMessageImpl(
    private val context: Context,
    private val cursorToPart: CursorToPart,
    private val keys: KeyManager,
    private val permissionManager: PermissionManager
) : CursorToMessage {

    companion object {
        private val uri = "content://mms-sms/complete-conversations".toUri()
        private val projection = arrayOf(
            MmsSms.TYPE_DISCRIMINATOR_COLUMN,
            MmsSms._ID,
            Mms.DATE,
            Mms.DATE_SENT,
            Mms.READ,
            Mms.THREAD_ID,
            Mms.LOCKED,

            Sms.ADDRESS,
            Sms.BODY,
            Sms.SEEN,
            Sms.TYPE,
            Sms.STATUS,
            Sms.ERROR_CODE,

            Mms.SUBJECT,
            Mms.SUBJECT_CHARSET,
            Mms.SEEN,
            Mms.MESSAGE_TYPE,
            Mms.MESSAGE_BOX,
            Mms.DELIVERY_REPORT,
            Mms.READ_REPORT,
            MmsSms.PendingMessages.ERROR_TYPE,
            Mms.STATUS
        )
    }

    override fun map(from: Pair<Cursor, CursorToMessage.MessageColumns>): Message {
        val cursor = from.first
        val columnsMap = from.second

        return Message().apply {
            type = when {
                cursor.getColumnIndex(MmsSms.TYPE_DISCRIMINATOR_COLUMN) != -1 -> cursor.getString(columnsMap.msgType)
                cursor.getColumnIndex(Mms.SUBJECT) != -1 -> "mms"
                cursor.getColumnIndex(Sms.ADDRESS) != -1 -> "sms"
                else -> "unknown"
            }

            id = keys.newId(CHANNEL_MESSAGE)
            threadId = cursor.getLong(columnsMap.threadId)
            contentId = cursor.getLong(columnsMap.msgId)
            date = cursor.getLong(columnsMap.date)
            dateSent = cursor.getLong(columnsMap.dateSent)
            read = cursor.getInt(columnsMap.read) != 0
            locked = cursor.getInt(columnsMap.locked) != 0
            subId = if (columnsMap.subId != -1) cursor.getInt(columnsMap.subId) else -1

            when (type) {
                "sms" -> {
                    address = cursor.getString(columnsMap.smsAddress) ?: ""
                    boxId = cursor.getInt(columnsMap.smsType)
                    seen = cursor.getInt(columnsMap.smsSeen) != 0

                    body = columnsMap.smsBody
                            .takeIf { column -> column != -1 } // The column may not be set
                            ?.let { column -> cursor.getString(column) } ?: "" // cursor.getString() may return null

                    errorCode = cursor.getInt(columnsMap.smsErrorCode)
                    deliveryStatus = cursor.getInt(columnsMap.smsStatus)
                }

                "mms" -> {
                    address = getMmsAddress(contentId)
                    boxId = cursor.getInt(columnsMap.mmsMessageBox)
                    date *= 1000L
                    dateSent *= 1000L
                    seen = cursor.getInt(columnsMap.mmsSeen) != 0
                    mmsDeliveryStatusString = cursor.getString(columnsMap.mmsDeliveryReport) ?: ""
                    errorType = if (columnsMap.mmsErrorType != -1) cursor.getInt(columnsMap.mmsErrorType) else 0
                    messageSize = 0
                    readReportString = cursor.getString(columnsMap.mmsReadReport) ?: ""
                    messageType = cursor.getInt(columnsMap.mmsMessageType)
                    mmsStatus = cursor.getInt(columnsMap.mmsStatus)
                    val subjectCharset = cursor.getInt(columnsMap.mmsSubjectCharset)
                    subject = cursor.getString(columnsMap.mmsSubject)
                            ?.takeIf { it.isNotBlank() }
                            ?.let(PduPersister::getBytes)
                            ?.let { EncodedStringValue(subjectCharset, it).string } ?: ""
                    textContentType = ""
                    attachmentType = Message.AttachmentType.NOT_LOADED

                    parts.addAll(cursorToPart.getPartsCursor(contentId)?.map { cursorToPart.map(it) } ?: listOf())
                }
            }
        }
    }

    override fun getMessagesCursor(dateFrom: Long): Cursor? {
        val projection = when (true /* TODO */) {
            true -> projection + Mms.SUBSCRIPTION_ID
            false -> projection
        }

        return when (permissionManager.hasReadSms()) {
            true -> SqliteWrapper.query(
                context,
                uri,
                projection,
                selection = "date >= $dateFrom",
                sortOrder = "normalized_date desc")
            false -> null
        }
    }

    override fun getMessageCursor(id: Long): Cursor? {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    private fun getMmsAddress(messageId: Long): String {
        val uri = Mms.CONTENT_URI.buildUpon()
                .appendPath(messageId.toString())
                .appendPath("addr").build()

        //TODO: Use Charset to ensure address is decoded correctly
        val projection = arrayOf(Mms.Addr.ADDRESS, Mms.Addr.CHARSET)
        val selection = "${Mms.Addr.TYPE} = ${PduHeaders.FROM}"

        val cursor = context.contentResolver.query(uri, projection, selection, null, null)
        cursor?.use {
            if (cursor.moveToFirst()) {
                return cursor.getString(0) ?: ""
            }
        }

        return ""
    }
}
