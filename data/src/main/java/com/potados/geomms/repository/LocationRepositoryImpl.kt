/*
 * Copyright (C) 2019-2020 Song Byeong Jun <potados99@gmail.com>
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

package com.potados.geomms.repository

import android.content.Context
import android.location.Location
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.potados.geomms.data.R
import com.potados.geomms.extension.nullOnFail
import com.potados.geomms.extension.unitOnFail
import timber.log.Timber

@SuppressWarnings("MissingPermission")
class LocationRepositoryImpl(
    context: Context
) : LocationRepository() {

    private var currentLocation: Location? = null

    private val locationClient = LocationServices.getFusedLocationProviderClient(context)

    private val request = LocationRequest.create().apply {
        interval = 15000            // 15 sec
        fastestInterval = 5000      // 5 sec
        priority = LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY
    }

    private val locationCallback = object: LocationCallback() {
        override fun onLocationResult(result: LocationResult?) = unitOnFail {
            if (result == null) {
                Timber.i("Location Result is null.")
                return@unitOnFail
            }

            // Get average of location data.
            currentLocation = Location(result.lastLocation).apply {
                latitude = result.locations.map { it.latitude }.average()
                longitude = result.locations.map { it.longitude }.average()
            }
        }
    }

    override fun getLocationWithCallback(onLocation: (Location) -> Unit) {
        locationClient
            .lastLocation
            .addOnSuccessListener {
                it?.let {
                    onLocation(it)
                } ?: fail(R.string.fail_turn_on_gps, show = true) // This happens when GPS is turned off.
            }
    }
    
    override fun getCurrentLocation(): Location? = nullOnFail {
        if (currentLocation == null) {
            Timber.i("Current location is null.")
        }

        return@nullOnFail currentLocation
    }

    override fun startLocationUpdates() = unitOnFail {
        locationClient.requestLocationUpdates(request, locationCallback, null)
    }

    override fun stopLocationUpdates() = unitOnFail{
        locationClient.removeLocationUpdates(locationCallback)
    }
}