package com.potados.geomms.feature.location.data

import android.location.Location

interface LocationRepository {
    fun getCurrentLocation(): Location?
}