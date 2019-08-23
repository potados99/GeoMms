package com.potados.geomms.service

import com.potados.geomms.model.Connection
import com.potados.geomms.model.ConnectionRequest
import com.potados.geomms.model.Packet
import io.realm.RealmResults

/**
 * Do communication thing.
 *
 * FORWARD:
 *  Terms:
 *      I       The user of this app.
 *      YOU     The other user. Could be a friend.
 */
interface LocationSupportService {



    fun getConnections(): RealmResults<Connection>

    fun getConnection(id: Long): Connection

    fun removeConnection(id: Long)

    fun getIncomingRequests(): RealmResults<ConnectionRequest>

    fun getOutgoingRequests(): RealmResults<ConnectionRequest>


    /** I request YOU to join. */
    fun requestNewConnection(address: String, duration: Long): ConnectionRequest
    /** YOU requested me to join. So I handle it. */
    fun beRequestedNewConnection(packet: Packet)

    /** I accept YOUr request. */
    fun acceptConnectionRequest(request: ConnectionRequest): Connection
    /** YOU accepted my request. I handle it. */
    fun beAcceptedConnectionRequest(packet: Packet)

    /** I refuse YOUr request. */
    fun refuseConnectionRequest(request: ConnectionRequest)
    /** YOU refused my request!. I handle it. */
    fun beRefusedConnectionRequest(packet: Packet)

    /** I send you an update. */
    fun sendUpdate(connectionId: Long)
    /** YOU sent me an update. I take it. */
    fun beSentUpdate(packet: Packet)

    /** I request you to send me an update. */
    fun requestUpdate(connectionId: Long)
    /** YOU requested me to send an update. I take care of it. */
    fun beRequestedUpdate(packet: Packet)

    /** I request YOU to disconnect. I already deleted the connection. */
    fun requestDisconnect(connectionId: Long)
    /** YOU requested me to disconnect. I handle it. */
    fun beRequestedDisconnect(packet: Packet)

    /** I send a packet to YOU. */
    fun sendPacket(address: String, packet: Packet)
    /** YOU sent me a packet. I handle it and call methods above. */
    fun receivePacket(address: String, body: String)

    fun parsePacket(body: String): Packet?

    fun serializePacket(packet: Packet): String?

    fun isValidPacket(body: String): Boolean
}