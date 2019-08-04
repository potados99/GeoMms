package com.potados.geomms.feature.common

import android.content.ContentUris
import android.content.Context
import android.net.Uri
import android.provider.ContactsContract
import kotlin.IllegalArgumentException

class ContactRepositoryImpl(
    private val context: Context
) : ContactRepository {

    override fun getContactNameByRecipientId(recipientId: Long): String? {
        val phoneNumberString = getPhoneNumberByRecipientId(recipientId)

        return getContactNameByPhoneNumber(phoneNumberString)
    }

    override fun getPhoneNumberByRecipientId(recipientId: Long): String {
        val uri = ContentUris.withAppendedId(CANONICAL_ADDRESSES_URI, recipientId)

        var phoneNumber: String? = null

        val cursor = context.contentResolver.query(uri, null, null, null, null)
            ?: throw IllegalArgumentException("Uri not exist for recipient id $recipientId.")

        if (cursor.moveToFirst()) {
            phoneNumber = cursor.getString(0)
        }

        cursor.close()

        return phoneNumber ?: throw IllegalArgumentException("Column zero of first row of $uri does not exist.")
    }

    override fun getContactNameByPhoneNumber(phoneNumber: String): String? {
        val uri = Uri.withAppendedPath(
            ContactsContract.PhoneLookup.CONTENT_FILTER_URI,
            Uri.encode(phoneNumber)
        )
        var contactName: String? = null

        val cursor = context.contentResolver.query(uri, arrayOf(ContactsContract.PhoneLookup.DISPLAY_NAME), null, null, null)
            ?: return null

        if (cursor.moveToFirst()) {
            contactName = cursor.getString(0)
        }

        cursor.close()

        return contactName
    }

    override fun getContactPhotoUriByPhoneNumber(phoneNumber: String): Uri? {
        val uri = Uri.withAppendedPath(
            ContactsContract.PhoneLookup.CONTENT_FILTER_URI,
            Uri.encode(phoneNumber)
        )

        var contactId: String? = null

        val cursor = context.contentResolver.query(uri, arrayOf(ContactsContract.PhoneLookup._ID), null, null, null)
            ?: return null

        if (cursor.moveToFirst()) {
            contactId = cursor.getString(0)
        }

        cursor.close()

        return ContentUris.withAppendedId(ContactsContract.Contacts.CONTENT_URI, contactId?.toLong() ?: return null)
    }

    companion object {
        val CANONICAL_ADDRESSES_URI: Uri = Uri.parse("content://mms-sms/canonical-address")
    }

}