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

package com.potados.geomms.model

import io.realm.RealmObject
import io.realm.annotations.Index
import io.realm.annotations.PrimaryKey
import java.util.*
import kotlin.collections.HashMap

open class Connection(
    @PrimaryKey var id: Long = 0,
    var recipient: Recipient? = null,
    var duration: Long = 0,
    var date: Long = 0, // date established or request sent(temporal)
    var lastUpdate: Long = 0,
    var lastSent: Long = 0,
    var latitude: Double = 0.0,
    var longitude: Double = 0.0,

    @Index var isTemporal: Boolean = false, // is isTemporal when not accepted yet.
    var isWaitingForReply: Boolean = false, // is waiting for recipient to send update?
    @Index var isOnTrack: Boolean = false // is this being tracked?

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