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

import android.net.Uri
import com.potados.geomms.model.Contact
import io.realm.RealmResults

abstract class ContactRepository : Repository() {

    abstract fun findContactUri(address: String): Uri?

    abstract fun getContacts(): RealmResults<Contact>?

    /**
     * Get recently connected contacts.
     */
    abstract fun getRecentContacts(): RealmResults<Contact>?

    abstract fun getUnmanagedContacts(): List<Contact>?
}