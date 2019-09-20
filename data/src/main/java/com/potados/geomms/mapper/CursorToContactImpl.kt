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

import android.content.Context
import android.database.Cursor
import android.provider.ContactsContract.CommonDataKinds.Phone.*
import com.potados.geomms.manager.PermissionManager
import com.potados.geomms.model.Contact
import com.potados.geomms.model.PhoneNumber

class CursorToContactImpl (
    private val context: Context,
    private val permissionManager: PermissionManager
) : CursorToContact {

    companion object {
        val URI = CONTENT_URI
        val PROJECTION = arrayOf(LOOKUP_KEY, NUMBER, TYPE, DISPLAY_NAME)

        const val COLUMN_LOOKUP_KEY = 0
        const val COLUMN_NUMBER = 1
        const val COLUMN_TYPE = 2
        const val COLUMN_DISPLAY_NAME = 3
    }

    override fun map(from: Cursor) = Contact().apply {
        lookupKey = from.getString(COLUMN_LOOKUP_KEY)
        name = from.getString(COLUMN_DISPLAY_NAME) ?: ""
        numbers.add(PhoneNumber().apply {
            address = from.getString(COLUMN_NUMBER) ?: ""
            type = context.getString(getTypeLabelResource(from.getInt(COLUMN_TYPE)))
        })
        lastUpdate = System.currentTimeMillis()
    }

    override fun getContactsCursor(): Cursor? {
        return when (permissionManager.hasContacts()) {
            true -> context.contentResolver.query(URI, PROJECTION, null, null, null)
            false -> null
        }
    }

}