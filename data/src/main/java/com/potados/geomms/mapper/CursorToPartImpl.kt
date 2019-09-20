/**
 * Copyright (C) 2019 Song Byeong Jun and original authors
 *
 * This file is part of GeoMms.
 *
 * This software makes use of third-party patent which belongs to
 * KANG MOON KYOU and LEE GWI BONG:
 * System and Method for sharing service of location information
 * 10-1235884-0000 (2013.02.15)
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

import android.content.ContentUris
import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.provider.Telephony
import com.potados.geomms.extension.*
import com.potados.geomms.model.MmsPart
import timber.log.Timber
import java.io.BufferedReader
import java.io.InputStreamReader

class CursorToPartImpl(
    private val context: Context
) : CursorToPart {

    companion object {
        val CONTENT_URI: Uri = Uri.parse("content://mms/part")
    }

    override fun map(from: Cursor) = MmsPart().apply {
        id = from.getLong(from.getColumnIndexOrThrow(Telephony.Mms.Part._ID))

        // Type will sometimes return null, resulting in a crash if we don't default to an empty string
        type = from.getString(from.getColumnIndexOrThrow(Telephony.Mms.Part.CONTENT_TYPE)) ?: ""

        val data = from.getString(from.getColumnIndexOrThrow(Telephony.Mms.Part._DATA))

        when {
            isSmil() || isImage() || isVideo() -> {
                // Do nothing special
            }

            isText() -> {
                text = if (data == null) {
                    from.getString(from.getColumnIndexOrThrow("text"))
                } else {
                    val uri = ContentUris.withAppendedId(CONTENT_URI, id)
                    val sb = StringBuilder()
                    val inputStream = tryOrNull(false) { context.contentResolver.openInputStream(uri) }

                    inputStream?.use {
                        val isr = InputStreamReader(inputStream, "UTF-8")
                        val reader = BufferedReader(isr)
                        var temp = reader.readLine()
                        while (temp != null) {
                            sb.append(temp)
                            temp = reader.readLine()
                        }
                    }

                    sb.toString()
                }
            }

            else -> Timber.v("Unhandled type: $type")
        }
    }

    override fun getPartsCursor(messageId: Long): Cursor? {
        return context.contentResolver.query(CONTENT_URI, null,
                "${Telephony.Mms.Part.MSG_ID} = ?", arrayOf(messageId.toString()), null)
    }

}