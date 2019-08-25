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
        interval = 10000 // 10sec
        fastestInterval = 2000 // 2sec
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