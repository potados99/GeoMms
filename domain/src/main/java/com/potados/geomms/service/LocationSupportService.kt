package com.potados.geomms.service

import com.potados.geomms.model.Connection
import com.potados.geomms.model.ConnectionRequest
import com.potados.geomms.model.Packet
import io.realm.RealmResults

/**
 * FOREWORD:
 * 1. This is not android.app.Service.
 * 2. Terms:
 *  I       The user of this app.
 *  YOU     The other user. Could be a friend.
 *
 * This class serves the main feature of this app:
 * Share location with friend(s) through SMS.
 *
 * It handles all things about the feature.
 * This service does:
 *  - Keep current location.
 *  - Send, cancel, accept, or refuse connection requests.
 *  - Send disconnection request.
 *  - Receive incoming packet and do proper job (e.g. notify user or handel it by itself or ignore)
 *  - Correct invalid requests or connections when they are required.
 *
 * To-do:
 * This architecture is fundamentally insecure because is uses non-encrypted method.
 * Do some encryption or notify user for implicitly harmful action.
 */
abstract class LocationSupportService : Service() {

    /**
     * All requests, connections are empty
     *
     * @return true if success, false if fail.
     */
    abstract fun isIdle(): Boolean

    /**
     * Process geomms messages in the inbox, which are not handled.
     *
     * @return true if success, false if fail.
     */
    abstract fun processUnhandledMessages(): Boolean

    /**
     * Disconnect all, refuse all, cancel all.
     *
     * @return true if all actions succeeded.
     */
    abstract fun clearAll(): Boolean

    /**
     * Request disconnect for all connections.
     *
     * @return true if success, false if fail.
     */
    abstract fun disconnectAll(): Boolean

    /**
     * Request refuse for all connections.
     *
     * @return true if success, false if fail.
     */
    abstract fun refuseAll(): Boolean

    /**
     * Request cancel for all sent-but-not-accepted requests.
     *
     * @return true if success, false if fail.
     */
    abstract fun cancelAll(): Boolean

    /**
     * Get all managed connections from realm.
     *
     * @return null if any exceptions.
     */
    abstract fun getConnections(): RealmResults<Connection>?

    /**
     * Get managed connection with id and temporal condition.
     *
     * @param id id of the connection.
     * @param temporal filter isTemporal. Null for don't care.
     *
     * @return null if any exceptions.
     */
    abstract fun getConnection(id: Long, temporal: Boolean? = null): Connection?

    /**
     * Get managed connection requests from realm with id and inbound connection.
     *
     * @param connectionId id of connection.
     * @param inbound filter isInbound
     *
     * @return null if any exceptions.
     */
    abstract fun getRequest(connectionId: Long, inbound: Boolean): ConnectionRequest?

    /**
     * Get all managed incoming(received) requests from realm.
     *
     * @return null if any exceptions.
     */
    abstract fun getIncomingRequests(): RealmResults<ConnectionRequest>?

    /**
     * Get all managed outgoing(sent) requests from realm.
     *
     * @return null if any exceptions.
     */
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
    abstract fun sendPacket(address: String, packet: Packet, postFix: String = ""): Boolean
    /** YOU sent me a packet. I handle it and call methods above. */
    abstract fun receivePacket(address: String, body: String): Boolean

    /**
     * Parse string to packet
     *
     * @return null if any exceptions or wrong packet form.
     */
    abstract fun parsePacket(body: String): Packet?

    /**
     * Serialize a packet to plain string.
     *
     * @return null if any exceptions or wrong packet.
     */
    abstract fun serializePacket(packet: Packet): String?

    /**
     * Check if the string is a valid packet form.
     *
     * @return true if this is a valid packet.
     */
    abstract fun isValidPacket(body: String): Boolean
}