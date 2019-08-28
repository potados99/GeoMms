package com.potados.geomms.service

import androidx.annotation.CallSuper
import com.potados.geomms.base.FailableComponent
import com.potados.geomms.base.Startable
import com.potados.geomms.model.Connection
import com.potados.geomms.model.ConnectionRequest
import com.potados.geomms.model.Packet
import io.realm.RealmResults
import timber.log.Timber

/**
 * Do communication thing.
 *
 * FORWARD
 *  Terms:
 *      I       The user of this app.
 *      YOU     The other user. Could be a friend.
 */
abstract class LocationSupportService : Service() {

    abstract fun clearConnections()

    abstract fun getConnections(): RealmResults<Connection>?

    abstract fun getConnection(id: Long, temporal: Boolean): Connection?

    abstract fun getRequest(connectionId: Long, inbound: Boolean): ConnectionRequest?

    abstract fun getIncomingRequests(): RealmResults<ConnectionRequest>?

    abstract fun getOutgoingRequests(): RealmResults<ConnectionRequest>?


    /** I request YOU to join. */
    abstract fun requestNewConnection(address: String, duration: Long)
    /** YOU requested me to join. So I handle it. */
    abstract fun beRequestedNewConnection(packet: Packet)

    /** I accept YOUr request. */
    abstract fun acceptConnectionRequest(request: ConnectionRequest)
    /** YOU accepted my request. I handle it. */
    abstract fun beAcceptedConnectionRequest(packet: Packet)

    /** I refuse YOUr request. */
    abstract fun refuseConnectionRequest(request: ConnectionRequest)
    /** YOU refused my request!. I handle it. */
    abstract fun beRefusedConnectionRequest(packet: Packet)

    /** I cancel my request to YOU */
    abstract fun cancelConnectionRequest(request: ConnectionRequest)
    /** YOU canceled YOUr request to me. */
    abstract fun beCanceledConnectionRequest(packet: Packet)

    /** I send you an update. */
    abstract fun sendUpdate(connectionId: Long)
    /** YOU sent me an update. I take it. */
    abstract fun beSentUpdate(packet: Packet)

    /** I request you to send me an update. */
    abstract fun requestUpdate(connectionId: Long)
    /** YOU requested me to send an update. I take care of it. */
    abstract fun beRequestedUpdate(packet: Packet)

    /** I request YOU to disconnect. I already deleted the connection. */
    abstract fun requestDisconnect(connectionId: Long)
    /** YOU requested me to disconnect. I handle it. */
    abstract fun beRequestedDisconnect(packet: Packet)

    /** I send a packet to YOU. */
    abstract fun sendPacket(address: String, packet: Packet)
    /** YOU sent me a packet. I handle it and call methods above. */
    abstract fun receivePacket(address: String, body: String)


    abstract fun parsePacket(body: String): Packet?

    abstract fun serializePacket(packet: Packet): String?

    abstract fun isValidPacket(body: String): Boolean?
}