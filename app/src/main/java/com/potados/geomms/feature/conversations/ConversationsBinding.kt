package com.potados.geomms.feature.conversations

import androidx.databinding.BindingAdapter
import androidx.recyclerview.widget.RecyclerView
import com.potados.geomms.model.Conversation
import io.realm.RealmResults
import timber.log.Timber


@BindingAdapter("conversations")
fun setConversations(listView: RecyclerView, conversations: RealmResults<Conversation>) {
    (listView.adapter as? ConversationsAdapter)?.let {
        it.updateData(conversations)
        Timber.i("conversations updated.")
    } ?: Timber.w("adapter not set.")
}