package com.potados.geomms.service

import com.potados.geomms.model.Connection
import com.potados.geomms.model.ConnectionRequest
import com.potados.geomms.model.Packet
import io.realm.RealmResults

/**
 * Do communication thing.
 *
 * FORWARD
 *  Terms:
 *      I       The user of this app.
 *      YOU     The other user. Could be a friend.
 */
abstract class LocationSupportService : Service() {

    /**
     * All requests, connections are empty
     */
    abstract fun isIdle(): Boolean

    /**
     * Disconnect all, refuse all, cancel all.
     */
    abstract fun clearAll(): Boolean

    abstract fun disconnectAll(): Boolean

    abstract fun refuseAll(): Boolean

    abstract fun cancelAll(): Boolean

    abstract fun getConnections(): RealmResults<Connection>?

    abstract fun getConnection(id: Long, temporal: Boolean): Connection?

    abstract fun getRequest(connectionId: Long, inbound: Boolean): ConnectionRequest?

    abstract fun getIncomingRequests(): RealmResults<ConnectionRequest>?

    abstract fun getOutgoingRequests(): RealmResults<ConnectionRequest>?


    /** I request YOU to join. */
    abstract fun requestNewConnection(address: String, duration: Long): Boolean
    /** YOU requested me to join. So I handle it. */
    abstract fun beRequestedNewConnection(packet: Packet): Boolean

    /** I accept YOUr request. */
    abstract fun acceptConnectionRequest(request: ConnectionRequest): Boolean
    /** YOU accepted my request. I handle it. */
    abstract fun beAcceptedConnectionRequest(packet: Packet): Boolean

    /** I refuse YOUr request. */
    abstract fun refuseConnectionRequest(request: ConnectionRequest): Boolean
    /** YOU refused my request!. I handle it. */
    abstract fun beRefusedConnectionRequest(packet: Packet): Boolean

    /** I cancel my request to YOU */
    abstract fun cancelConnectionRequest(request: ConnectionRequest): Boolean
    abstract fun cancelConnectionRequest(temporalConnection: Connection): Boolean
    /** YOU canceled YOUr request to me. */
    abstract fun beCanceledConnectionRequest(packet: Packet): Boolean

    /** I send you an update. */
    abstract fun sendUpdate(connectionId: Long): Boolean
    /** YOU sent me an update. I take it. */
    abstract fun beSentUpdate(packet: Packet): Boolean

    /** I request you to send me an update. */
    abstract fun requestUpdate(connectionId: Long): Boolean
    /** YOU requested me to send an update. I take care of it. */
    abstract fun beRequestedUpdate(packet: Packet): Boolean

    /** I request YOU to disconnect. I already deleted the connection. */
    abstract fun requestDisconnect(connectionId: Long): Boolean
    /** YOU requested me to disconnect. I handle it. */
    abstract fun beRequestedDisconnect(packet: Packet): Boolean

    /** I send a packet to YOU. */
    abstract fun sendPacket(address: String, packet: Packet): Boolean
    /** YOU sent me a packet. I handle it and call methods above. */
    abstract fun receivePacket(address: String, body: String): Boolean


    abstract fun parsePacket(body: String): Packet?

    abstract fun serializePacket(packet: Packet): String?

    abstract fun isValidPacket(body: String): Boolean?
}