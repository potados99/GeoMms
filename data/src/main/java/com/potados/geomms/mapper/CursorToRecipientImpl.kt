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
import android.net.Uri
import com.potados.geomms.manager.PermissionManager
import com.potados.geomms.model.Recipient

class CursorToRecipientImpl(
    private val context: Context,
    private val permissionManager: PermissionManager
) : CursorToRecipient {

    companion object {
        val URI = Uri.parse("content://mms-sms/canonical-addresses")

        const val COLUMN_ID = 0
        const val COLUMN_ADDRESS = 1
    }

    override fun map(from: Cursor) = Recipient().apply {
        id = from.getLong(COLUMN_ID)
        address = from.getString(COLUMN_ADDRESS)
        lastUpdate = System.currentTimeMillis()
    }

    override fun getRecipientCursor(): Cursor? {
        return when (permissionManager.hasReadSms()) {
            true -> context.contentResolver.query(URI, null, null, null, null)
            false -> null
        }
    }

    override fun getRecipientCursor(id: Long): Cursor? {
        return context.contentResolver.query(URI, null, "_id = ?", arrayOf(id.toString()), null)
    }

}