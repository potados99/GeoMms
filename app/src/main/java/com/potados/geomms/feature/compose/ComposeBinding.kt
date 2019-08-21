package com.potados.geomms.feature.compose

import androidx.databinding.BindingAdapter
import androidx.recyclerview.widget.RecyclerView
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
