/**
 * Copyright (C) 2019 Song Byeong Jun and original authors
 *
 * This file is part of GeoMms.
 *
 * This software makes use of third-party patent which belongs to
 * KANG MOON KYOU and LEE GWI BONG:
 * System and Method for sharing service of location information
 * 10-1235884-0000 (2013.02.15)
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

package com.potados.geomms.model

import io.realm.RealmObject
import io.realm.annotations.Index
import io.realm.annotations.PrimaryKey

open class Connection(
    @PrimaryKey var id: Long = 0,
    var recipient: Recipient? = null,
    var duration: Long = 0,
    var date: Long = 0, // date established or request sent(temproal)
    var lastUpdate: Long = 0,
    var lastSent: Long = 0,
    var latitude: Double = 0.0,
    var longitude: Double = 0.0,

    @Index var isTemporal: Boolean = false // is isTemporal when not accepted yet.

) : RealmObject() {

    fun isExpired(): Boolean =
        (System.currentTimeMillis() - date > duration)

    val due: Long
        get() = date + duration

    val timeLeft: Long
        get() = due - System.currentTimeMillis()

    companion object {
        fun fromAcceptedRequest(request: ConnectionRequest) =
            Connection(
                id = request.connectionId,
                recipient = request.recipient,
                duration = request.duration,
                date = System.currentTimeMillis()
            )
    }
}