package com.potados.geomms.manager

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.potados.geomms.model.Connection
import io.realm.Realm
import timber.log.Timber

class MapManagerImpl : MapManager() {

    private val coordinateLive = MutableLiveData<Pair<Double, Double>>().apply {
        value = null
    }

    override fun triggerShowOnMap(lat: Double, lng: Double) {
        coordinateLive.postValue(Pair(lat, lng))

        Timber.i("Triggered to show $lat:$lng on map.")
    }

    override fun showOnMapEvent(): LiveData<Pair<Double, Double>> {
        return coordinateLive
    }

    override fun setTracking(connectionId: Long?) {
        val realm =  Realm.getDefaultInstance()

        val connectionsOnTrack = realm.where(Connection::class.java)
            .equalTo("isOnTrack", true)
            .findAll()

        realm.use {
            it.executeTransaction {
                connectionsOnTrack.forEach { connection ->
                    connection.isOnTrack = false
                }

                connectionId?.let {
                    val connectionToTrack = realm.where(Connection::class.java)
                        .equalTo("id", connectionId)
                        .findFirst()

                    connectionToTrack?.isOnTrack = true
                }
            }
        }

        Timber.i("Connection $connectionId is now being tracked.")
    }
}