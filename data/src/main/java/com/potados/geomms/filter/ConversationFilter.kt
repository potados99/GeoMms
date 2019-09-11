
package com.potados.geomms.filter

import com.potados.geomms.model.Conversation

class ConversationFilter(private val recipientFilter: RecipientFilter) : Filter<Conversation>() {

    override fun filter(item: Conversation, query: CharSequence): Boolean {
        return item.recipients.any { recipient -> recipientFilter.filter(recipient, query) }
    }

}