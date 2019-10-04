package com.potados.geomms.manager

import androidx.lifecycle.LiveData

abstract class MapManager : Manager() {
    abstract fun triggerShowOnMap(lat: Double, lng: Double)
    abstract fun showOnMapEvent(): LiveData<Pair<Double, Double>>

    abstract fun setTracking(connectionId: Long?)
}