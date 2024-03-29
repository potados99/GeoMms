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

package com.potados.geomms.mapper

import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.provider.Telephony
import android.provider.Telephony.Threads
import com.potados.geomms.manager.PermissionManager
import com.potados.geomms.model.Conversation
import com.potados.geomms.model.Recipient
import com.potados.geomms.util.SqliteWrapper

class CursorToConversationImpl (
    private val context: Context,
    private val permissionManager: PermissionManager
) : CursorToConversation {

    companion object {
        val uri: Uri = Telephony.MmsSms.CONTENT_CONVERSATIONS_URI
            .buildUpon()
            .appendQueryParameter("simple", "true")
            .build()

        val projection = arrayOf(
                Threads._ID,
                Threads.DATE,
                Threads.RECIPIENT_IDS,
                Threads.MESSAGE_COUNT,
                Threads.READ,
                Threads.SNIPPET
        )

        const val ID = 0
        const val DATE = 1
        const val RECIPIENT_IDS = 2
        const val MESSAGE_COUNT = 3
        const val READ = 4
        const val SNIPPET = 5
    }

    override fun map(from: Cursor): Conversation {
        return Conversation().apply {
            id = from.getLong(ID)
            date = from.getLong(DATE)
            recipients.addAll(from.getString(RECIPIENT_IDS)
                    .split(" ")
                    .filter { it.isNotBlank() }
                    .map { recipientId -> recipientId.toLong() }
                    .map { recipientId -> Recipient().apply { id = recipientId } })
            count = from.getInt(MESSAGE_COUNT)
            read = from.getInt(READ) == 1
            snippet = from.getString(SNIPPET) ?: ""
        }
    }

    override fun getConversationsCursor(dateFrom: Long): Cursor? {
        return when (permissionManager.hasReadSms()) {
            true -> SqliteWrapper.query(
                context,
                uri,
                projection,
                selection = "date >= $dateFrom",
                sortOrder = "date desc")
            false -> null
        }
    }

}