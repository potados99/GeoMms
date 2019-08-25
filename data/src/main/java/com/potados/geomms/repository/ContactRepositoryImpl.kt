/*
 * Copyright (C) 2017 Moez Bhatti <moez.bhatti@gmail.com>
 *
 * This file is part of QKSMS.
 *
 * QKSMS is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * QKSMS is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with QKSMS.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.potados.geomms.repository

import android.content.Context
import android.net.Uri
import android.provider.BaseColumns
import android.provider.ContactsContract
import android.provider.ContactsContract.CommonDataKinds.Email
import android.provider.ContactsContract.CommonDataKinds.Phone
import com.potados.geomms.model.Contact
import io.realm.Realm
import io.realm.RealmResults

class ContactRepositoryImpl(private val context: Context) : ContactRepository {

    override fun findContactUri(address: String): Uri? {
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

        return cursor?.use {
            val id = cursor.getString(cursor.getColumnIndexOrThrow(BaseColumns._ID))

            Uri.withAppendedPath(ContactsContract.Contacts.CONTENT_URI, id)
        }
    }

    override fun getContacts(): RealmResults<Contact> {
        val realm = Realm.getDefaultInstance()
        return realm.where(Contact::class.java)
                .sort("name")
                .findAll()
    }

    override fun getUnmanagedContacts(): List<Contact> {
        val realm = Realm.getDefaultInstance()

        val mobileLabel by lazy {
            Phone.getTypeLabel(context.resources, Phone.TYPE_MOBILE, "Mobile").toString()
        }

        return realm.where(Contact::class.java)
            .contains("numbers.type", mobileLabel)
            .sort("name")
            .findAll()
            .map { realm.copyFromRealm(it) }
    }
}