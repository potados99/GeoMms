package com.potados.geomms.util

import android.provider.ContactsContract.PhoneLookup
import android.content.Context
import android.net.Uri


class ContactHelper {
    companion object {
        fun getContactName(context: Context, phoneNumber: String): String? {
            val resolver = context.contentResolver
            val uri = Uri.withAppendedPath(
                PhoneLookup.CONTENT_FILTER_URI,
                Uri.encode(phoneNumber)
            )
            val cursor = resolver.query(uri, arrayOf(PhoneLookup.DISPLAY_NAME), null, null, null) ?: return null

            var contactName: String? = null

            if (cursor.moveToFirst()) {
                contactName = cursor.getString(cursor.getColumnIndex(PhoneLookup.DISPLAY_NAME))
            }

            cursor.close()

            return contactName
        }
    }
}