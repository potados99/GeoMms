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

package com.potados.geomms.feature.compose

import androidx.databinding.BindingAdapter
import androidx.recyclerview.widget.RecyclerView
import com.potados.geomms.model.Attachments
import com.potados.geomms.model.Contact
import com.potados.geomms.model.Conversation
import com.potados.geomms.model.Message
import io.realm.RealmResults
import timber.log.Timber

@BindingAdapter("messages")
fun setMessages(listView: RecyclerView, messages: RealmResults<Message>?) {
    (listView.adapter as? MessagesAdapter)?.let { adapter ->
        messages?.let {
            adapter.updateData(it)
            Timber.i("messages updated.")
        } ?: Timber.i("messages are not set yet.")

    } ?: Timber.w("adapter not set.")
}

@BindingAdapter("conversation")
fun setConversation(listView: RecyclerView, conversation: Conversation?) {
    (listView.adapter as? MessagesAdapter)?.let { adapter ->
        conversation?.let {
            adapter.conversation = it
            Timber.i("conversation updated.")
        } ?: Timber.i("conversation is not set yet.")

    } ?: Timber.w("adapter not set.")
}

@BindingAdapter("contacts")
fun setContacts(listView: RecyclerView, data: List<Contact>?) {
    (listView.adapter as? ContactAdapter)?.let {
        it.data = data.orEmpty()
        Timber.i("Contacts updated.")
    } ?: Timber.w("Adapter not set.")
}

@BindingAdapter("attachments")
fun setAttachments(listView: RecyclerView, data: Attachments?) {
    (listView.adapter as? AttachmentAdapter)?.let {
        it.data = data.orEmpty()
        Timber.i("Attachments updated.")
    } ?: Timber.w("Adapter not set.")
}