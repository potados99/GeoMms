/**
 * Copyright (C) 2019 Song Byeong Jun and original authors
 *
 * This file is part of GeoMms.
 *
 * This software makes use of third-party patent which belongs to
 * KANG MOON KYOU and LEE GWI BONG:
 * System and Method for sharing service of location information
 * 10-1235884-0000 (2013.02.15)
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
        Timber.i("Connections updated.")
    } ?: Timber.w("Adapter not set.")
}

@BindingAdapter("incoming_requests")
fun setIncomingRequests(listView: RecyclerView, requests: RealmResults<ConnectionRequest>) {
    (listView.adapter as? RequestsAdapter)?.let {
        it.updateData(requests)
        Timber.i("Requests updated.")
    } ?: Timber.w("Adapter not set.")
}