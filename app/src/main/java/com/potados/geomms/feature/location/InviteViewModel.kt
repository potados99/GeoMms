package com.potados.geomms.feature.location

import androidx.lifecycle.ViewModel
import com.potados.geomms.feature.compose.filter.ContactFilter
import com.potados.geomms.model.Contact
import com.potados.geomms.repository.ContactRepository
import org.koin.core.KoinComponent
import org.koin.core.inject

class InviteViewModel : ViewModel(), KoinComponent {
    // Repository
    private val contactRepo: ContactRepository by inject()

    // Filter
    private val contactFilter: ContactFilter by inject()

    fun getContacts(query: String = ""): List<Contact> {
        return contactRepo.getUnmanagedContacts().filter {
            contactFilter.filter(it, query)
        }
    }

    // TODO
    fun setContact(contact: Contact) {

    }
}