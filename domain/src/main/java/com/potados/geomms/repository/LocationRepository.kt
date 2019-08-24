package com.potados.geomms.repository

import android.location.Location

interface LocationRepository {
    fun getCurrentLocation(): Location?

    fun startLocationUpdates()

    fun stopLocationUpdates()
}