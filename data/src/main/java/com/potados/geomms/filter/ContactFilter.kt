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

package com.potados.geomms.filter

import com.potados.geomms.extension.removeAccents
import com.potados.geomms.model.Contact
import timber.log.Timber

class ContactFilter(private val phoneNumberFilter: PhoneNumberFilter) : Filter<Contact>() {

    override fun filter(item: Contact, query: CharSequence): Boolean {
        // Remember to apply removeAccents to the query TOO.
        // Or the query will not match the normalized unicode string.
        return item.name.removeAccents().contains(query.removeAccents(), true) || // Name
                item.numbers.map { it.address }.any { address -> phoneNumberFilter.filter(address, query) } // Number
    }

}