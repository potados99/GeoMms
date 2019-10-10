/*
 * Copyright (C) 2019 Song Byeong Jun <potados99@gmail.com>
 *
 * This file is part of GeoMms.
 *
 * GeoMms is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * GeoMms is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with GeoMms.  If not, see <http://www.gnu.org/licenses/>.
 */

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