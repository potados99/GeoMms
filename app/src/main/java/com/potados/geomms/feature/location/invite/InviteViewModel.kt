/*
 * Copyright (C) 2019 Song Byeong Jun <potados99@gmail.com>
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

package com.potados.geomms.feature.location.invite

import android.content.Intent
import android.telephony.PhoneNumberUtils
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.MutableLiveData
import com.potados.geomms.R
import com.potados.geomms.common.base.BaseViewModel
import com.potados.geomms.feature.location.MapFragment
import com.potados.geomms.filter.ContactFilter
import com.potados.geomms.model.Contact
import com.potados.geomms.model.PhoneNumber
import com.potados.geomms.repository.ContactRepository
import com.potados.geomms.service.LocationSupportService
import io.realm.RealmList
import org.koin.core.KoinComponent
import org.koin.core.inject
import java.util.*

class InviteViewModel : BaseViewModel(), KoinComponent {

    private val service: LocationSupportService by inject()
    private val contactRepo: ContactRepository by inject()

    private val contactFilter: ContactFilter by inject()

    val contacts = MutableLiveData<List<Contact>>().apply {
        value = getContacts()
    }

    init {
        failables += this
        failables += contactRepo
        failables += contactFilter
    }

    fun onSearch(query: CharSequence?) {
        val queryString = query.toString()

        var contacts = getContacts(queryString)

        if (PhoneNumberUtils.isWellFormedSmsAddress(queryString)) {
            val newAddress = PhoneNumberUtils.formatNumber(queryString, Locale.getDefault().country)
            val newContact = Contact(numbers = RealmList(PhoneNumber(address = newAddress ?: queryString)))
            contacts = listOf(newContact) + contacts
        }

        this.contacts.value = contacts
    }

    fun onContactClick(activity: FragmentActivity?, contact: Contact) {
        val address= contact.numbers[0]?.address

        if (address == null) {
            fail(R.string.fail_cannot_select_address_not_exist, show = true)
        } else {
            activity?.sendBroadcast(
                Intent(MapFragment.ACTION_SET_ADDRESS).putExtra(MapFragment.EXTRA_ADDRESS, address)
            )
        }

        activity?.finish()
    }

    private fun getContacts(query: String = ""): List<Contact> {
        val contacts = contactRepo.getUnmanagedContacts()

        if (contacts == null) {
            fail(R.string.fail_get_contacts, true)
            return listOf()
        }

        val invitedContacts = service.getOutgoingRequests()?.mapNotNull{ it.recipient?.contact } ?: listOf()
        val askedContacts = service.getIncomingRequests()?.mapNotNull{ it.recipient?.contact } ?: listOf()
        val connectedContacts = service.getConnections()?.mapNotNull { it.recipient?.contact } ?: listOf()

        return contacts.filter { contact ->
            contactFilter.filter(contact, query) &&
                    contact.lookupKey !in (invitedContacts + askedContacts + connectedContacts).map { it.lookupKey }
        }
    }
}