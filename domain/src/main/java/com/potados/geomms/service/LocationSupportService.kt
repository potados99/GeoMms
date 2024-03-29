/*
 * Copyright (C) 2019-2020 Song Byeong Jun <potados99@gmail.com>
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
    abstract fun getRequest(connectionId: Long, inbound: Boolean?): ConnectionRequest?

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

    /** Can I invite this guy? without duplicates? */
    abstract fun canInvite(address: String): Boolean

    /** I request YOU to join. */
    abstract fun requestNewConnection(address: String, duration: Long): Boolean
    abstract fun requestNewConnectionAgain(address: String, duration: Long, id: Long): Boolean
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

    /**
     * Parse string to packet.
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