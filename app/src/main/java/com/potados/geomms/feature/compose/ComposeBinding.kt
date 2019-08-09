package com.potados.geomms.feature.compose

import androidx.databinding.BindingAdapter
import androidx.recyclerview.widget.RecyclerView
import com.potados.geomms.model.Message
import io.realm.RealmResults

@BindingAdapter("messages")
fun setMessages(listView: RecyclerView, messages: RealmResults<Message>) {
    (listView.adapter as? MessagesAdapter)?.updateData(messages)
}
