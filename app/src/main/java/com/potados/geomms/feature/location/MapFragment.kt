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

import android.content.IntentFilter
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapsInitializer
import com.google.android.gms.maps.OnMapReadyCallback
import com.potados.geomms.R
import com.potados.geomms.common.base.NavigationFragment
import com.potados.geomms.common.extension.*
import com.potados.geomms.databinding.MapFragmentBinding
import com.potados.geomms.util.Popup
import kotlinx.android.synthetic.main.connections_fragment.view.*
import kotlinx.android.synthetic.main.map_fragment.view.*
import timber.log.Timber

class MapFragment : NavigationFragment(), OnMapReadyCallback {

    override val optionMenuId: Int? = R.menu.map
    override val navigationItemId: Int = R.id.menu_item_navigation_map
    override val titleId: Int = R.string.title_friends

    private lateinit var mapViewModel: MapViewModel
    private lateinit var viewDataBinding: MapFragmentBinding

    private var map: GoogleMap? = null

    /**
     * Invoked when ACTION_SET_ADDRESS intent received.
     * It is needed because this fragment and invite fragment has no connection.
     */
    private val addressSetReceiver = newBroadcastReceiver {
        it?.getStringExtra(EXTRA_ADDRESS)?.let { address ->
            // Default 30 min.
            var duration = 1000L * 60 * 30

            Popup(activity)
                .withTitle(R.string.title_duration)
                .withSingleChoiceItems(R.array.durations, 0) { selected ->
                    duration = when (selected) {
                        0 -> 1000L * 60 * 30
                        1 -> 1000L * 60 * 60
                        2 -> 1000L * 60 * 180
                        else -> throw RuntimeException("This is IMPOSSIBLE. Check your code.")
                    }
                }
                .withPositiveButton(R.string.button_confirm) {
                    mapViewModel.request(activity, address, duration)
                }
                .show()
        }
    }

    /**
     * Initialization for connection list sheet.
     */
    private val initializeSheetView: (ConnectionsFragment, View) -> Unit = { fragment, sheetView ->
        with(sheetView) {
            // Setting a behavior of the bottom sheet MUST take place
            // at where the parent of the sheet is available.
            bottomSheetBehavior.addCallback(
                // TODO
                // This code makes bottom sheet scroll slower.
                // onSlide = { empty_view.setVerticalBiasByOffset(it) }
            )

            connections.makeThisWorkInBottomSheet(sheetView)
            incoming_requests.makeThisWorkInBottomSheet(sheetView)
        }

        fragment.onShowConnectionOnMap = {
            if (it.lastUpdate != 0L) {
                map?.moveTo(it.latitude, it.longitude, 15f)
            }
        }
    }

    init {
        failables += this
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        MapsInitializer.initialize(context)

        mapViewModel = getViewModel { start() }
        failables += mapViewModel.failables

        context?.registerReceiver(addressSetReceiver, IntentFilter(ACTION_SET_ADDRESS))
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return MapFragmentBinding
            .inflate(inflater, container, false)
            .apply { vm = mapViewModel }
            .apply { viewDataBinding = this }
            .apply { initializeView(root, savedInstanceState) }
            .root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        // Add connections list sheet.
        childBottomSheetManager
            ?.push(ConnectionsFragment(), cancelable = false)
            ?.apply {
                observe(isInitialized) {
                    if (it == true) {
                        initializeSheetView(childFragment as ConnectionsFragment, sheetView)
                    }
                }
            }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.invite -> {
                mapViewModel.invite()
            }
        }

        return true
    }

    override fun onShow() {
        super.onShow()
        Timber.i("MapFragment is shown")
    }
    override fun onHide() {
        super.onHide()
        Timber.i("MapFragment is hidden")
    }
    override fun onResume() {
        super.onResume()
        viewDataBinding.mapView.onResume()
    }
    override fun onPause() {
        super.onPause()
        viewDataBinding.mapView.onPause()
    }
    override fun onDestroy() {
        super.onDestroy()
        viewDataBinding.mapView.onDestroy()
        context?.unregisterReceiver(addressSetReceiver)
    }
    override fun onLowMemory() {
        super.onLowMemory()
        viewDataBinding.mapView.onLowMemory()
    }

    override fun onMapReady(map: GoogleMap?) {
        this.map = map?.apply {

            // Set map UI.
            try {
                isMyLocationEnabled = true

                uiSettings.isCompassEnabled = true
                uiSettings.isMyLocationButtonEnabled = true
            } catch (e: SecurityException) {
                fail(R.string.fail_exception, e.message, show = false)
            }

            // Move to current location.
            mapViewModel.getLocation {
                moveTo(it.latitude, it.longitude, 10f, bias = false)
            }

            // Hide bottom sheet when map moving.
            setOnCameraMoveStartedListener {
                if (it == GoogleMap.OnCameraMoveStartedListener.REASON_GESTURE) {
                    childBottomSheetManager?.collapseSheet()

                    // Stop tracking for friends location.
                    mapViewModel.untrack()
                }
            }

            // Draw marker when connection refreshed.
            observe(mapViewModel.markers) { markers ->
                clear() // map
                markers?.forEach { marker ->
                    addMarker(marker) // map

                    Timber.i("Marker drawn.")
                }
            }
        }

        Timber.i( "Map is ready!")
    }

    private fun initializeView(view: View, savedInstanceState: Bundle?) {
        with(view.map_view) {
            onCreate(savedInstanceState)
            getMapAsync(this@MapFragment) // onMapReady called after this done
        }
    }


    companion object {
        const val ACTION_SET_ADDRESS = "com.potados.geomms.SET_ADDR"
        const val EXTRA_ADDRESS = "address"
    }
}
