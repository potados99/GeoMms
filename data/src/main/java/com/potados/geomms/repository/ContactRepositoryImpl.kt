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
import io.realm.Sort

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

    override fun getRecentContacts(): RealmResults<Contact>? = nullOnFail {
        val realm = Realm.getDefaultInstance()
        return@nullOnFail realm.where(Contact::class.java)
            .greaterThan("lastConnected", 0)
            .sort("lastConnected", Sort.DESCENDING)
            .limit(5)
            .findAll()
    }

    override fun getUnmanagedContacts(): List<Contact>? = nullOnFail {
        val mobileLabel by lazy {
            Phone.getTypeLabel(context.resources, Phone.TYPE_MOBILE, "Mobile").toString()
        }

        return@nullOnFail Realm.getDefaultInstance().use { realm ->
            realm
                .where(Contact::class.java)
                .contains("numbers.type", mobileLabel)
                .sort(arrayOf("lastConnected", "name"), arrayOf(Sort.DESCENDING, Sort.ASCENDING))
                .findAll()
                .map { realm.copyFromRealm(it) }
        }
    }
}