package com.potados.geomms.repository

import android.content.Context
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.util.Log
import com.google.android.gms.location.LocationServices
import com.potados.geomms.manager.PermissionManager
import timber.log.Timber

@SuppressWarnings("MissingPermission")
class LocationRepositoryImpl(
    context: Context
) : LocationRepository {

    private val locationClient = LocationServices.getFusedLocationProviderClient(context)

    private var currentLocation: Location? = null

    init {
        locationClient.lastLocation.addOnSuccessListener { location: Location? ->
            currentLocation = location
        }
    }

    override fun getCurrentLocation(): Location? {
        if (currentLocation == null) {
            Timber.i("Current location is null.")
        }
        return currentLocation
    }
}