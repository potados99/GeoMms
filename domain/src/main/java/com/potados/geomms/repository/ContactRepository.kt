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