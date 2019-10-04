/*
 * Copyright (C) 2019 Song Byeong Jun <potados99@gmail.com>
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

package com.potados.geomms.feature.location

import android.location.Location
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.MutableLiveData
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.potados.geomms.R
import com.potados.geomms.common.base.BaseViewModel
import com.potados.geomms.common.navigation.Navigator
import com.potados.geomms.common.util.DateFormatter
import com.potados.geomms.manager.TrackingManager
import com.potados.geomms.model.Connection
import com.potados.geomms.model.ConnectionRequest
import com.potados.geomms.repository.LocationRepository
import com.potados.geomms.service.LocationSupportService
import com.potados.geomms.util.Popup
import io.realm.RealmResults
import org.koin.core.KoinComponent
import org.koin.core.inject
import timber.log.Timber
import java.util.*

/**
 * View Model for [MapFragment].
 *
 * @see [MapFragment]
 */
class MapViewModel : BaseViewModel(), KoinComponent {

    private val service: LocationSupportService by inject()
    private val locationRepo: LocationRepository by inject()
    private val trackManager: TrackingManager by inject()
    private val dateFormatter: DateFormatter by inject()
    private val navigator: Navigator by inject()

    val connections = service.getConnections()

    private val _markers = mutableListOf<MarkerOptions>()
    val markers =  MutableLiveData<MutableList<MarkerOptions>>()

    private val refreshMarkers = { connections: RealmResults<Connection> ->
        _markers.clear()
        connections.forEach {
            if (it.lastUpdate != 0L) {
                // do not display marker when having nothing to display.

                val update = dateFormatter.getConversationTimestamp(it.lastUpdate)
                val lastUpdateString = context.getString(R.string.connection_last_update, update)

                val markerOption = MarkerOptions()
                    .position(LatLng(it.latitude, it.longitude))
                    .title(it.recipient?.getDisplayName())
                    .snippet(lastUpdateString)
                    .icon(BitmapDescriptorFactory.defaultMarker(Random(it.id).nextInt(360).toFloat()))

                _markers.add(markerOption)

                Timber.i("Marker added.")
            }
        }

        // commit
        markers.value = _markers
    }

    init {
        failables += this
        failables += service
        failables += locationRepo
        failables += dateFormatter
        failables += navigator
    }

    override fun start() {
        super.start()

        connections?.let {
            // Setting change listener does not invoke it on register time.
            // Do it for one time manually.
            it.addChangeListener(refreshMarkers)
            refreshMarkers(it)
        }
    }

    /**
     * Show contacts to invite.
     */
    fun invite() {
        navigator.showInvite()
    }

    /**
     * Send request
     */
    fun request(activity: FragmentActivity?, address: String, duration: Long) {
        if (service.requestNewConnection(address, duration)) {
            Popup(activity)
                .withTitle(R.string.title_invitation_sent)
                .withMessage(R.string.connection_request_sent_to, address)
                .withPositiveButton(R.string.button_ok)
                .show()
        }
    }

    fun getLocation(onLocation: (Location) -> Unit) {
        locationRepo.getLocationWithCallback(onLocation)
    }

    fun untrack() {
        trackManager.setTracking(null)
    }
}