
package com.potados.geomms.filter

import android.telephony.PhoneNumberUtils

class PhoneNumberFilter : Filter<String>() {

    override fun filter(item: String, query: CharSequence): Boolean {
        val allCharactersDialable = query.all { PhoneNumberUtils.isReallyDialable(it) }

        return allCharactersDialable && (PhoneNumberUtils.compare(item, query.toString()) ||
                PhoneNumberUtils.stripSeparators(item).contains(PhoneNumberUtils.stripSeparators(query.toString())))
    }

}