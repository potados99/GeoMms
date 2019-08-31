package com.potados.geomms.repository

import android.location.Location

abstract class LocationRepository : Repository() {
    abstract fun getLocationWithCallback(onLocation: (Location) -> Unit)

    abstract fun getCurrentLocation(): Location?

    abstract fun startLocationUpdates()

    abstract fun stopLocationUpdates()
}