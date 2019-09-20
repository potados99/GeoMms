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

package com.potados.geomms.repository

import android.content.Context
import android.location.Location
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
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
        priority = LocationRequest.PRIORITY_HIGH_ACCURACY
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
            .addOnSuccessListener(onLocation)
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