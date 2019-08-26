package com.potados.geomms.feature.location

import com.potados.geomms.base.Failable
import com.potados.geomms.common.base.BaseViewModel
import com.potados.geomms.filter.ContactFilter
import com.potados.geomms.model.Contact
import com.potados.geomms.repository.ContactRepository
import org.koin.core.KoinComponent
import org.koin.core.inject

class InviteViewModel : BaseViewModel(), KoinComponent {

    private val contactRepo: ContactRepository by inject()

    private val contactFilter: ContactFilter by inject()

    override fun start() {
        super.start()

        failables.addAll(
            listOf(
                this,
                contactRepo,
                contactFilter
            )
        )
    }

    fun getContacts(query: String = ""): List<Contact> {
        val contacts = contactRepo.getUnmanagedContacts()?.filter {
            contactFilter.filter(it, query)
        }

        if (contacts == null) {
            setFailure(Failable.Failure("Failed to get contacts.", true))
            return listOf()
        }

        return contacts
    }

    // TODO
    fun setContact(contact: Contact) {

    }
}