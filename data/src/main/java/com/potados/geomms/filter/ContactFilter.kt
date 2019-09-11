
package com.potados.geomms.filter

import com.potados.geomms.extension.removeAccents
import com.potados.geomms.model.Contact

class ContactFilter(private val phoneNumberFilter: PhoneNumberFilter) : Filter<Contact>() {

    override fun filter(item: Contact, query: CharSequence): Boolean {
        return item.name.removeAccents().contains(query, true) || // Name
                item.numbers.map { it.address }.any { address -> phoneNumberFilter.filter(address, query) } // Number
    }

}