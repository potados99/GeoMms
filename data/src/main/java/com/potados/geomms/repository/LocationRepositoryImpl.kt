package com.potados.geomms.repository

import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.util.Log
import com.potados.geomms.manager.PermissionManager
import timber.log.Timber

@SuppressWarnings("MissingPermission")
class LocationRepositoryImpl(
    locationManager: LocationManager,
    permissionManager: PermissionManager,
    minTime: Long = 1000,
    minDistance: Float = 5.0f
) : LocationRepository, LocationListener {

    private var currentLocation: Location? = null

    init {
        try {
            if (permissionManager.hasLocation()) {
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, minTime, minDistance, this)
                locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, minTime, minDistance, this)
            } else {
                Timber.w("Location permission missing.")
            }
        }
        catch (e: Exception) {
            Timber.e(e)
            throw e
        }
    }

    override fun getCurrentLocation(): Location? {
        if (currentLocation == null) {
            Timber.i("Current location is null.")
        }
        return currentLocation
    }

    override fun onLocationChanged(location: Location?) {
        currentLocation = location

        Timber.i("Location changed.")
    }

    override fun onProviderDisabled(provider: String?) {
        Timber.i("Provider($provider) disabled.")
    }

    override fun onProviderEnabled(provider: String?) {
        Timber.i("Provider($provider) enabled.")
    }

    override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {
        Timber.i("Provider($provider) status changed: $status.")
    }

}