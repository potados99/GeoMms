package com.potados.geomms.model

import android.location.Location
import com.potados.geomms.util.DateTime
import io.realm.RealmObject
import io.realm.annotations.PrimaryKey

open class Connection(
    @PrimaryKey var id: Long = 0,
    var recipient: Recipient? = null,
    var duration: Long = 0,
    var date: Long = 0, // date established
    var lastUpdate: Long = 0,
    var lastSent: Long = 0,
    var latitude: Double = 0.0,
    var longitude: Double = 0.0

) : RealmObject() {

    fun isExpired(): Boolean =
        (System.currentTimeMillis() - date > duration)

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