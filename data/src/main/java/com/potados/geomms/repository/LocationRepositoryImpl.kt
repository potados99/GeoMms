package com.potados.geomms.repository

import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.util.Log
import timber.log.Timber

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

            locationManager.requestLocationUpdates(
                LocationManager.NETWORK_PROVIDER, minTime, minDistance, this
            )
        }
        catch (e: SecurityException) {
            Log.e("LocationRepositoryImpl:init", "permission denied")
            throw e
        }
        catch (e: Exception) {
            // Timber.w(e)
        }
    }

    override fun getCurrentLocation(): Location? {
        Log.i("LocationRepositoryImpl:getLocation", "location is ${if (currentLocation != null) "not null" else "null"}")
        return currentLocation
    }
    override fun onLocationChanged(location: Location?) {
        currentLocation = location
        Log.d("LocationRepositoryImpl:onLocationChanged", "location changed.")
    }

    override fun onProviderDisabled(provider: String?) {
    }

    override fun onProviderEnabled(provider: String?) {
    }

    override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {
    }

}