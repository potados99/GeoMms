package com.potados.geomms.model

import io.realm.RealmObject
import io.realm.annotations.Index
import io.realm.annotations.PrimaryKey

open class Connection(
    @PrimaryKey var id: Long = 0,
    var recipient: Recipient? = null,
    var duration: Long = 0,
    var date: Long = 0, // date established
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