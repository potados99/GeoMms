package com.potados.geomms.feature.location.invite

import androidx.databinding.BindingAdapter
import androidx.recyclerview.widget.RecyclerView
import com.potados.geomms.feature.compose.ContactAdapter
import com.potados.geomms.model.Contact
import timber.log.Timber

@BindingAdapter("recents")
fun setRecents(listView: RecyclerView, data: List<Contact>?) {
    (listView.adapter as? ContactAdapter)?.let {
        it.data = data.orEmpty()
        Timber.i("Recent contacts updated.")
    } ?: Timber.w("Adapter not set.")
}