package com.potados.geomms.feature.data.implementation

import android.content.ContentResolver
import android.content.ContentUris
import android.content.Context
import android.net.Uri
import android.provider.ContactsContract
import com.potados.geomms.feature.data.repository.ContactRepository
import java.lang.IllegalArgumentException

class ContactRepositoryImpl(
    private val context: Context
) : ContactRepository {

    override fun getContactNameByRecipientId(recipientId: Long): String? {
        val phoneNumberString = getPhoneNumberByRecipientId(recipientId)
            ?: throw IllegalArgumentException("Wrong recipientId.")

        return getContactNameByPhoneNumber(phoneNumberString)
    }

    override fun getPhoneNumberByRecipientId(recipientId: Long): String? {
        val uri = ContentUris.withAppendedId(CANONICAL_ADDRESSES_URI, recipientId)

        var phoneNumber: String? = null

        context.contentResolver.query(uri, null, null, null, null)?.apply {

            if (moveToFirst()) {
                phoneNumber = getString(0)
            }

            close()

        } ?: return null

        return phoneNumber
    }

    override fun getContactNameByPhoneNumber(phoneNumber: String): String? {
        val uri = Uri.withAppendedPath(
            ContactsContract.PhoneLookup.CONTENT_FILTER_URI,
            Uri.encode(phoneNumber)
        )
        var contactName: String? = null

        context.contentResolver.query(uri, arrayOf(ContactsContract.PhoneLookup.DISPLAY_NAME), null, null, null)?.apply {
            if (moveToFirst()) {
                contactName = getString(getColumnIndex(ContactsContract.PhoneLookup.DISPLAY_NAME))
            }

            close()

        } ?: return null

        return contactName
    }

    companion object {
        val CANONICAL_ADDRESSES_URI = Uri.parse("content://mms-sms/canonical-address")
    }

}