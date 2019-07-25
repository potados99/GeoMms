package com.potados.geomms.feature.location.data

import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle

class LocationRepositoryImpl(
    locationManager: LocationManager,
    minTime: Long = 1000,
    minDistance: Float = 5.0f
) : LocationRepository, LocationListener {

    private var currentLocation: Location? = null

    init {
        try {
            locationManager.requestLocationUpdates(
                LocationManager.GPS_PROVIDER, minTime, minDistance, this
            )
        }
        catch (e: SecurityException) {
            throw e
        }
    }

    override fun getCurrentLocation(): Location? = currentLocation

    override fun onLocationChanged(location: Location?) {
        currentLocation = location
    }

    override fun onProviderDisabled(provider: String?) {
    }

    override fun onProviderEnabled(provider: String?) {
    }

    override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {
    }
}