package com.potados.geomms.feature.compose

import androidx.databinding.BindingAdapter
import androidx.recyclerview.widget.RecyclerView
import com.potados.geomms.model.Message
import io.realm.RealmResults
import timber.log.Timber

@BindingAdapter("messages")
fun setMessages(listView: RecyclerView, messages: RealmResults<Message>) {
    (listView.adapter as? MessagesAdapter)?.updateData(messages)
        ?: Timber.w("adapter not set.")
    
    Timber.i("messages updated.")
}
