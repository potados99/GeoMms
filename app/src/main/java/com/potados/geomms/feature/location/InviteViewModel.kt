package com.potados.geomms.feature.location

import androidx.lifecycle.ViewModel
import com.potados.geomms.common.base.BaseViewModel
import com.potados.geomms.feature.compose.filter.ContactFilter
import com.potados.geomms.model.Contact
import com.potados.geomms.repository.ContactRepository
import org.koin.core.KoinComponent
import org.koin.core.inject

class InviteViewModel : BaseViewModel(), KoinComponent {

    private val contactRepo: ContactRepository by inject()

    private val contactFilter: ContactFilter by inject()

    override fun start() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    fun getContacts(query: String = ""): List<Contact> {
        return contactRepo.getUnmanagedContacts().filter {
            contactFilter.filter(it, query)
        }
    }

    // TODO
    fun setContact(contact: Contact) {

    }
}