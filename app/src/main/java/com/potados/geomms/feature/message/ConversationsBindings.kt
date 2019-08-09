package com.potados.geomms.feature.message

import androidx.databinding.BindingAdapter
import androidx.recyclerview.widget.RecyclerView
import com.potados.geomms.model.Conversation
import io.realm.RealmResults

@BindingAdapter("conversations")
fun setConversations(listView: RecyclerView, conversations: RealmResults<Conversation>?) {
    conversations?.let {
        (listView.adapter as? ConversationsAdapter)?.updateData(conversations)
    }
}