package com.potados.geomms.util

import android.content.ContentResolver
import android.provider.ContactsContract.PhoneLookup
import android.net.Uri
import android.content.ContentUris
import java.lang.IllegalArgumentException


class ContactHelper {
    companion object {
        fun getContactNameByRecipientId(resolver: ContentResolver, recipientId: Long): String? {
            val phoneNumberString = getPhoneNumberByRecipientId(resolver, recipientId)
                ?: throw IllegalArgumentException("Wrong recipientId.")

            return getContactNameByPhoneNumber(resolver, phoneNumberString)
        }

        fun getPhoneNumberByRecipientId(resolver: ContentResolver, recipientId: Long): String? {
            val uri = ContentUris.withAppendedId(Uri.parse("content://mms-sms/canonical-address"), recipientId)

            var phoneNumber: String? = null

            resolver.query(uri, null, null, null, null)?.apply {

                if (moveToFirst()) {
                    phoneNumber = getString(0)
                }

                close()

            } ?: return null

            return phoneNumber
        }

        fun getContactNameByPhoneNumber(resolver: ContentResolver, phoneNumber: String): String? {
            val uri = Uri.withAppendedPath(
                PhoneLookup.CONTENT_FILTER_URI,
                Uri.encode(phoneNumber)
            )
            var contactName: String? = null

            resolver.query(uri, arrayOf(PhoneLookup.DISPLAY_NAME), null, null, null)?.apply {
                if (moveToFirst()) {
                    contactName = getString(getColumnIndex(PhoneLookup.DISPLAY_NAME))
                }

                close()

            } ?: return null

            return contactName
        }
    }
}