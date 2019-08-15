package com.potados.geomms.service

import com.potados.geomms.model.Connection
import com.potados.geomms.model.ConnectionRequest
import com.potados.geomms.model.Packet
import com.potados.geomms.model.Recipient
import io.realm.Realm
import io.realm.RealmResults

interface LocationSupportService {

    fun getConnections(): RealmResults<Connection>

    fun getConnection(id: Long): Connection

    fun removeConnection(id: Long)

    fun getIncomingRequests(): RealmResults<ConnectionRequest>

    fun getOutgoingRequests(): RealmResults<ConnectionRequest>

    fun requestNewConnection(address: String, duration: Long): ConnectionRequest

    fun acceptConnectionRequest(request: ConnectionRequest): Connection


    fun sendUpdate(connectionId: Long)

    fun requestUpdate(connectionId: Long)

    fun requestDisconnect(connectionId: Long)


    fun sendPacket(address: String, packet: Packet)

    fun receivePacket(address: String, body: String)

    fun parsePacket(body: String): Packet?

    fun serializePacket(packet: Packet): String?

    fun isValidPacket(body: String): Boolean
}