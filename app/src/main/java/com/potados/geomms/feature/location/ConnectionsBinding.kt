package com.potados.geomms.feature.location

import androidx.databinding.BindingAdapter
import androidx.recyclerview.widget.RecyclerView
import com.potados.geomms.model.Connection
import com.potados.geomms.model.ConnectionRequest
import io.realm.RealmResults
import timber.log.Timber

@BindingAdapter("connections")
fun setConnections(listView: RecyclerView, connections: RealmResults<Connection>) {
    (listView.adapter as? ConnectionsAdapter)?.let {
        it.updateData(connections)
        Timber.i("connections updated.")
    } ?: Timber.w("adapter not set.")
}

@BindingAdapter("incoming_requests")
fun setIncomingRequests(listView: RecyclerView, requests: RealmResults<ConnectionRequest>) {
    (listView.adapter as? RequestsAdapter)?.let {
        it.updateData(requests)
        Timber.i("requests updated.")
    } ?: Timber.w("adapter not set.")
}