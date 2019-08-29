package com.potados.geomms.repository

import android.content.Context
import android.net.Uri
import android.provider.BaseColumns
import android.provider.ContactsContract
import android.provider.ContactsContract.CommonDataKinds.Email
import android.provider.ContactsContract.CommonDataKinds.Phone
import com.potados.geomms.extension.nullOnFail
import com.potados.geomms.model.Contact
import io.realm.Realm
import io.realm.RealmResults

class ContactRepositoryImpl(private val context: Context) : ContactRepository() {

    override fun findContactUri(address: String): Uri? = nullOnFail {
        val uri = when {
            address.contains('@') -> {
                Uri.withAppendedPath(Email.CONTENT_FILTER_URI, Uri.encode(address))
            }

            else -> {
                Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI, Uri.encode(address))
            }
        }

        val cursor = context.contentResolver.query(
            uri, arrayOf(BaseColumns._ID), null, null, null
        )

        return@nullOnFail cursor?.use {
            val id = cursor.getString(cursor.getColumnIndexOrThrow(BaseColumns._ID))

            Uri.withAppendedPath(ContactsContract.Contacts.CONTENT_URI, id)
        }
    }

    override fun getContacts(): RealmResults<Contact>? = nullOnFail {
        val realm = Realm.getDefaultInstance()
        return@nullOnFail realm.where(Contact::class.java)
            .sort("name")
            .findAll()
    }

    override fun getUnmanagedContacts(): List<Contact>? = nullOnFail {
        val realm = Realm.getDefaultInstance()

        val mobileLabel by lazy {
            Phone.getTypeLabel(context.resources, Phone.TYPE_MOBILE, "Mobile").toString()
        }

        return@nullOnFail realm.where(Contact::class.java)
            .contains("numbers.type", mobileLabel)
            .sort("name")
            .findAll()
            .map { realm.copyFromRealm(it) }
    }
}