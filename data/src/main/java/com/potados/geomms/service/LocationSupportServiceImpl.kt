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

package com.potados.geomms.service

import android.content.Context
import android.content.Intent
import android.telephony.SmsManager
import androidx.annotation.StringRes
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.google.gson.JsonSyntaxException
import com.potados.geomms.base.Failable
import com.potados.geomms.data.R
import com.potados.geomms.extension.falseOnFail
import com.potados.geomms.extension.nullOnFail
import com.potados.geomms.extension.unitOnFail
import com.potados.geomms.manager.KeyManager
import com.potados.geomms.model.*
import com.potados.geomms.receiver.SendUpdateReceiver
import com.potados.geomms.receiver.SendUpdateReceiver.Companion.EXTRA_CONNECTION_ID
import com.potados.geomms.repository.ConversationRepository
import com.potados.geomms.repository.LocationRepository
import com.potados.geomms.usecase.DeleteMessages
import com.potados.geomms.usecase.DeleteMessages.Params
import com.potados.geomms.util.DateTime
import com.potados.geomms.util.Reflection
import com.potados.geomms.util.Scheduler
import com.potados.geomms.util.Types
import io.realm.Realm
import io.realm.RealmObject
import io.realm.RealmResults
import io.realm.Sort
import timber.log.Timber

/**
 * Implementation of [LocationSupportService].
 *
 * All methods are covered by [unitOnFail] or [falseOnFail] or [nullOnFail].
 * None of these methods will throw exceptions.
 *
 * If somethings got wrong and it should be noticed by the user,
 * a [Failable.Failure] should be set.
 *
 * @see [LocationSupportService]
 * @see [unitOnFail]
 * @see [falseOnFail]
 * @see [nullOnFail]
 */
class LocationSupportServiceImpl(
    private val context: Context,
    private val deleteMessages: DeleteMessages,
    private val conversationRepo: ConversationRepository,
    private val locationRepo: LocationRepository,
    private val scheduler: Scheduler,
    private val keyManager: KeyManager
) : LocationSupportService() {

    private var started = false

    /**
     * This will be used for validating requests and connections.
     */
    private val validator = Validator()


    /************************************
     * STATE CONTROL
     ************************************/

    override fun start() = unitOnFail {
        if (started) {
            Timber.w("Already started!")
            return@unitOnFail
        }
        super.start()

        validator.validateAll()
        processUnhandledMessages()
        restoreTasks()

        started = true

        Timber.i("Service started.")
    }

    override fun isIdle(): Boolean {
        return getConnections()?.isEmpty() == true
                && getIncomingRequests()?.isEmpty() == true
                && getOutgoingRequests()?.isEmpty() == true
    }

    /**
     * Find messages that contain GEO_MMS_PREFIX,
     * process and remove them,
     * and return true if all succeeded of nothing exists.
     */
    override fun processUnhandledMessages() = falseOnFail {
        return@falseOnFail getRealm().use { realm ->
            realm.where(Message::class.java)
                .contains("body", GEO_MMS_PREFIX) // It only searched for SMS.
                .findAll() // Consider using findAllAsync.
                .let { messages -> messages.takeIf { it.isNotEmpty() } ?: return@falseOnFail true } // cool
                .map { managed -> realm.copyFromRealm(managed) }
                .onEach { copied -> deleteMessages(Params(listOf(copied.id))) } // awesome
                .map { copied -> receivePacket(copied.address, copied.body) }
                .reduce { acc, b -> acc && b }
        }
    }

    override fun clearAll() = falseOnFail {
        return@falseOnFail listOf(disconnectAll(), refuseAll(), cancelAll())
            .takeIf { it.isNotEmpty() }
            ?.reduce { acc, b -> acc && b }
            ?: false
    }

    override fun disconnectAll() = falseOnFail {
        return@falseOnFail getConnections()
            ?.filter { !it.isTemporal }
            ?.takeIf { it.isNotEmpty() }
            ?.map { requestDisconnect(it.id) }
            ?.reduce { acc, b -> acc && b }
            ?: false
    }

    override fun refuseAll() = falseOnFail {
        return@falseOnFail getIncomingRequests()
            ?.takeIf { it.isNotEmpty() }
            ?.map { refuseConnectionRequest(it) }
            ?.reduce { acc, b -> acc && b }
            ?: false
    }

    override fun cancelAll() = falseOnFail {
        return@falseOnFail getOutgoingRequests()
            ?.takeIf { it.isNotEmpty() }
            ?.map { cancelConnectionRequest(it) }
            ?.reduce { acc, b -> acc && b }
            ?: false
    }


    /************************************
     * INTERNAL STATE CONTROL
     ************************************/

    /**
     * Request location updates.
     * Not need anymore because we use getLocationWithCallback() instead.
     *
     * @see [LocationRepository]
     */
    private fun startLocationUpdates() = falseOnFail {
        locationRepo.startLocationUpdates()
        return@falseOnFail true
    }


    /************************************
     * GETTER
     ************************************/

    override fun getConnections(): RealmResults<Connection>? = nullOnFail{
        return@nullOnFail getRealm().where(Connection::class.java)
            .sort("date", Sort.ASCENDING)
            .findAll()
    }


    /**
     * Auto correct connection without recipient.
     */
    override fun getConnection(id: Long, temporal: Boolean?): Connection? = nullOnFail {
        return@nullOnFail getRealm()
            .where(Connection::class.java)
            .equalTo("id", id)
            .let {
                if (temporal != null) it.equalTo("isTemporal", temporal)
                else it
            }
            .findFirst()
    }

    override fun getRequest(connectionId: Long, inbound: Boolean): ConnectionRequest? = nullOnFail {
        return@nullOnFail getRealm()
            .where(ConnectionRequest::class.java)
            .equalTo("connectionId", connectionId)
            .equalTo("isInbound", inbound)
            .findFirst()
    }

    private fun getRecipient(address: String): Recipient? = nullOnFail {
        // If not exist, that's a problem.
        val recipient = conversationRepo.getOrCreateConversation(address)?.recipients?.get(0)

        if (recipient == null) {
            fail(R.string.fail_no_recipient, address, show = true)
            return@nullOnFail null
        }

        return@nullOnFail recipient
    }

    override fun getIncomingRequests(): RealmResults<ConnectionRequest>? = nullOnFail {
        return@nullOnFail  getRealm()
            .where(ConnectionRequest::class.java)
            .sort("date", Sort.ASCENDING)
            .equalTo("isInbound", true)
            .findAll()
    }
    override fun getOutgoingRequests(): RealmResults<ConnectionRequest>? = nullOnFail {
        return@nullOnFail  getRealm().where(ConnectionRequest::class.java)
            .sort("date", Sort.ASCENDING)
            .equalTo("isInbound", false)
            .findAll()
    }


    /************************************
     * INTERNAL GETTER
     ************************************/

    /**
     * Construct a request from an inbound request packet.
     */
    private fun requestFromInboundPacket(packet: Packet): ConnectionRequest? = nullOnFail {
        if (!packet.isInbound) {
            fail(R.string.fail_packet_not_inbound, show = true)
            return@nullOnFail null
        }

        return@nullOnFail ConnectionRequest(
            connectionId = packet.connectionId,
            recipient = getRecipient(packet.address),
            isInbound = packet.isInbound,
            date = if (packet.isInbound) packet.date else packet.dateSent,
            duration = packet.duration
        )
    }

    override fun canInvite(address: String) = falseOnFail {
        val invited = getOutgoingRequests()?.mapNotNull{ it.recipient } ?: listOf()
        val asked = getIncomingRequests()?.mapNotNull{ it.recipient } ?: listOf()
        val connected = getConnections()?.mapNotNull { it.recipient } ?: listOf()

        return@falseOnFail address !in (invited + asked + connected).map { it.address }
    }

    override fun requestNewConnection(address: String, duration: Long) = falseOnFail {
        val recipient = getRecipient(address) ?: return@falseOnFail false

        // We have to prevent duplicated invitation.
        // If the target already has been invited? break.
        // If the target invited you? break.
        // If the target is already connected? break.
        if (!canInvite(recipient.address)) {
            // This is a duplication or error.
            Timber.w("Cannot invite $address. Duplicated.")
            fail(R.string.fail_already_invited, address, show = true)

            return@falseOnFail false
        }

        val request = ConnectionRequest(
            connectionId = keyManager.randomId(99999),
            recipient = recipient,
            isInbound = false, // outbound
            date = System.currentTimeMillis(), // now
            duration = duration
        )

        // Add google play store link at the end of the serialized packet.
        val postfix = "\nhttp://play.google.com/store/apps/details?id=" + context.packageName

        sendPacket(address, Packet.ofRequestingNewConnection(request), postfix)

        // Add this not-yet accepted connection to getRealm.
        val temporalConnection = Connection.fromAcceptedRequest(request).apply { isTemporal = true }

        executeInDefaultInstance { realm ->
            recipient.contact?.let {
                realm.insertOrUpdate(it.apply {
                    lastConnected = System.currentTimeMillis()
                })
            }
            realm.insertOrUpdate(request)
            realm.insertOrUpdate(temporalConnection)
        }

        Timber.i("Requested connection to ${recipient.getDisplayName()} with id ${request.connectionId}.")

        return@falseOnFail true
    }
    override fun beRequestedNewConnection(packet: Packet) = falseOnFail {
        // requests can be duplicated but connections cannot.
        getConnection(packet.connectionId, temporal = false)?.let {
            fail(R.string.fail_ignore_illegal_request, show = false)
            return@falseOnFail false
        }

        val request = requestFromInboundPacket(packet)

        if (request == null) {
            fail(R.string.fail_cannot_obtain_request, show = true)
            return@falseOnFail false
        }

        executeInDefaultInstance { it.insertOrUpdate(request) }

        Timber.i("New incoming request from ${request.recipient?.getDisplayName()} with id ${request.connectionId}.")

        return@falseOnFail true
    }

    override fun acceptConnectionRequest(request: ConnectionRequest) = falseOnFail {
        val validated = validator.validate(request) {
            it.isInbound && getConnection(it.connectionId, false) == null
        }

        // If is not inbound or connection already exists.
        if (validated == null) {
            fail(R.string.fail_cannot_accept_request_invalid, show = true)
            return@falseOnFail false
        }

        // Notify accepted.
        request.recipient?.let {
            sendPacket(it.address, Packet.ofAcceptingRequest(request))
        }

        val connection = Connection.fromAcceptedRequest(request)

        // add connection, delete request
        executeInDefaultInstance { realm ->
            request.deleteFromRealm()

            realm.insertOrUpdate(connection)

            registerTask(connection.id)
        }

        // start sending updates

        Timber.i("Accept -> New connection(${connection.id}) established with ${connection.id}.")

        return@falseOnFail true
    }
    override fun beAcceptedConnectionRequest(packet: Packet) = falseOnFail {
        val request = validator.validate(getRequest(packet.connectionId, inbound = false)) {
            getConnection(it.connectionId, false) == null
        }

        if (request == null) {
            fail(R.string.fail_ignore_wrong_accept, show = false)
            return@falseOnFail false
        }

        // if there is a temporal connection already added, it will be updated.
        val connection = Connection.fromAcceptedRequest(request)

        executeInDefaultInstance { realm ->
            request.deleteFromRealm()

            realm.insertOrUpdate(connection)
            registerTask(connection.id)
        }

        Timber.i("Accepted -> New connection(${connection.id}) established with ${connection.id}.")

        return@falseOnFail true
    }

    override fun refuseConnectionRequest(request: ConnectionRequest) = falseOnFail {
        val validated = validator.validate(request) {
            it.isInbound && getConnection(it.connectionId, false) == null
        }

        // If is not inbound or connection already exists.
        if (validated == null) {
            fail(R.string.fail_cannot_refuse_request_invalid, show = true)
            return@falseOnFail false
        }

        // let you know
        request.recipient?.let {
            sendPacket(it.address, Packet.ofRefusingRequest(request))
        }

        // delete
        executeInDefaultInstance {
            request.deleteFromRealm()
        }

        Timber.i("Refused request of id ${request.connectionId} from ${request.recipient?.getDisplayName()}.")

        return@falseOnFail true
    }
    override fun beRefusedConnectionRequest(packet: Packet) = falseOnFail {
        val temporalConnection = validator.validate(getConnection(packet.connectionId, temporal = true))

        val request = validator.validate(getRequest(packet.connectionId, inbound = false))  {
            getConnection(it.connectionId, false) == null
        }

        if (request == null) {
            fail(R.string.fail_ignore_wrong_refuse, show = false)
            return@falseOnFail false
        }

        // TOOD: There could be some cases
        // where request is gone but the temporal connection stays valid.

        Timber.i("Request of id ${request.connectionId} to ${request.recipient?.getDisplayName()} is refused.")

        executeInDefaultInstance {
            request.deleteFromRealm()
            temporalConnection?.deleteFromRealm()
        }

        return@falseOnFail true
    }

    override fun cancelConnectionRequest(request: ConnectionRequest) = falseOnFail {
        val temporalConnection = validator.validate(getConnection(request.connectionId, temporal = true))

        val validated = validator.validate(request) {
            !it.isInbound && getConnection(it.connectionId, false) == null
        }

        if (validated == null) {
            fail(R.string.fail_cannot_cancel_request_invalid, show = true)
            return@falseOnFail false
        }

        // TOOD: There could be some cases
        // where request is gone but the temporal connection stays valid.

        // Notify canceled.
        request.recipient?.let {
            sendPacket(it.address, Packet.ofCancelingRequest(request))
        }

        Timber.i("Cancel request of id ${request.connectionId} to ${request.recipient?.getDisplayName()}")

        executeInDefaultInstance {
            request.deleteFromRealm()
            temporalConnection?.deleteFromRealm()
        }

        return@falseOnFail true
    }
    override fun cancelConnectionRequest(temporalConnection: Connection) = falseOnFail {
        val validated = validator.validate(temporalConnection) { it.isTemporal }

        if (validated == null) {
            fail(R.string.fail_cannot_cancel_request_temp_connection_invalid, show = true)
            return@falseOnFail false
        }

        val request = validator.validate(getRequest(temporalConnection.id, inbound = false))

        if (request == null) {
            fail(R.string.fail_cannot_cancel_request_invalid, show = true)
            return@falseOnFail false
        }

        cancelConnectionRequest(request)

        return@falseOnFail true
    }
    override fun beCanceledConnectionRequest(packet: Packet) = falseOnFail {
        val request = validator.validate(getRequest(packet.connectionId, inbound = true))  {
            getConnection(it.connectionId, false) == null
        }

        if (request == null) {
            fail(R.string.fail_ignore_wrong_cancel, show = false)
            return@falseOnFail false
        }

        Timber.i("Request of id ${request.connectionId} from ${request.recipient?.getDisplayName()} is canceled by the sender.")

        executeInDefaultInstance { request.deleteFromRealm() }

        return@falseOnFail true
    }

    override fun sendUpdate(connectionId: Long) = falseOnFail {
        val connection = validator.validate(getConnection(connectionId, temporal = false))

        if (connection == null) {
            fail(R.string.fail_cannot_send_update_invalid_connection, show = true)
            return@falseOnFail false
        }

        locationRepo.getLocationWithCallback { location ->
            // On location success

            // We validate it again for two reason:
            // 1. It is not sure the connection is valid at this moment.
            // 2. Task might be ran on different thread.
            val connectionAgain = validator.validate(getConnection(connectionId, temporal = false))

            if (connectionAgain == null) {
                fail(R.string.fail_cannot_send_update_invalid_connection, show = true)
            } else {
                val packet = Packet.ofSendingData(connectionAgain, location)

                connectionAgain.recipient?.address?.let {
                    sendPacket(it, packet)
                }

                executeInDefaultInstance {
                    connectionAgain.lastSent = System.currentTimeMillis()
                }

                Timber.i("Sent update.")
            }
        }

        return@falseOnFail true
    }
    override fun beSentUpdate(packet: Packet) = falseOnFail {
        val connection = validator.validate(getConnection(packet.connectionId, temporal = false))

        if (connection == null) {
            fail(R.string.fail_ignore_wrong_data, show = false)
            return@falseOnFail false
        }

        executeInDefaultInstance {
            connection.lastUpdate = System.currentTimeMillis()
            connection.latitude = packet.latitude
            connection.longitude = packet.longitude
        }

        Timber.i("Received update of connection ${connection.id} from ${connection.recipient?.getDisplayName()}.")

        return@falseOnFail true
    }

    override fun requestUpdate(connectionId: Long) = falseOnFail {
        val connection = getConnection(connectionId, temporal = false)

        if (connection == null) {
            fail(R.string.fail_cannot_request_update_invalid_connection, show = true)
            return@falseOnFail false
        }

        val packet = Packet.ofRequestingUpdate(connection)

        sendPacket(connection.recipient?.address ?: return@falseOnFail false, packet)

        Timber.i("Requested update of ${connection.id} to ${connection.recipient?.getDisplayName()}.")

        return@falseOnFail true
    }
    override fun beRequestedUpdate(packet: Packet) = falseOnFail {
        val connection = validator.validate(getConnection(packet.connectionId, false))

        if (connection == null) {
            // This could be an illegal try.
            // Think how to handle it. (e.g. notify user with warning)
            fail(R.string.fail_ignore_wrong_update_request, show = false)
            return@falseOnFail false
        }

        // We have to reply to the update request
        // for three times, with each delay for 1 sec.
        scheduleSendUpdate(connection.id, 3, 1000)

        Timber.i("Send update of connection ${connection.id} in response of ${connection.recipient?.getDisplayName()}'s request.")

        return@falseOnFail true
    }

    override fun requestDisconnect(connectionId: Long) = falseOnFail {
        val connection = validator.validate(getConnection(connectionId, temporal = false))

        if (connection == null) {
            fail(R.string.fail_cannot_request_disconnect_invalid_connection, show = true)
            return@falseOnFail false
        }

        // Stop sending updates.
        unregisterTask(connection.id)

        val packet = Packet.ofRequestingDisconnect(connection)

        sendPacket(connection.recipient?.address ?: return@falseOnFail false, packet)

        Timber.i("Requested disconnect of connection ${connection.id} to ${connection.recipient?.getDisplayName()}")

        executeInDefaultInstance {
            connection.deleteFromRealm()
        }

        return@falseOnFail true
    }
    override fun beRequestedDisconnect(packet: Packet) = falseOnFail {
        val connection = validator.validate(getConnection(packet.connectionId, false))

        if (connection == null) {
            fail(R.string.fail_ignore_wrong_disconnect, show = false)
            return@falseOnFail false
        }

        sendPacket(connection.recipient?.address ?: return@falseOnFail false, packet)

        Timber.i("Disconnect and delete connection of id ${connection.id} in response of ${connection.recipient?.getDisplayName()}'s request.")

        executeInDefaultInstance {
            connection.deleteFromRealm()
        }

        return@falseOnFail true
    }


    /************************************
     * PACKET PROCESS
     ************************************/

    override fun sendPacket(address: String, packet: Packet, postFix: String) = falseOnFail {
        val serialized = serializePacket(packet) ?: return@falseOnFail false

        val payload = serialized + postFix

        SmsManager.getDefault().sendTextMessage(
            address,
            null,
            payload,
            null,
            null
        )

        Timber.i("Sent packet: \"$payload\"")

        return@falseOnFail true
    }
    override fun receivePacket(address: String, body: String) = falseOnFail {
        val packet = parsePacket(body)?.apply {
            this.address = address
            this.isInbound = true
        } ?: return@falseOnFail false

        Timber.i("Received packet is ${findType(packet.type).toString()}")

        val result = when (packet.type) {

            Packet.PacketType.REQUEST_CONNECT.number -> {
                beRequestedNewConnection(packet)
            }

            Packet.PacketType.ACCEPT_CONNECT.number -> {
                beAcceptedConnectionRequest(packet)
            }

            Packet.PacketType.REFUSE_CONNECT.number -> {
                beRefusedConnectionRequest(packet)
            }

            Packet.PacketType.CANCEL_CONNECT.number -> {
                beCanceledConnectionRequest(packet)
            }

            Packet.PacketType.TRANSFER_DATA.number -> {
                beSentUpdate(packet)
            }

            Packet.PacketType.REQUEST_DATA.number -> {
                beRequestedUpdate(packet)
            }

            Packet.PacketType.REQUEST_DISCONNECT.number -> {
                beRequestedDisconnect(packet)
            }

            else -> {
                Timber.w("Packet type ${packet.type} not found.")
                false
            }
        }

        if (result) {
            Timber.i("Successfully handled packet.")
        }

        return@falseOnFail result
    }

    /**
     * Create a packet from a plain string.
     *
     * Fields of packet is dependent on type of packet.
     * The packet type is at the first field(fixed) of packet.
     *
     * Once the packet type is specified,
     * we know the meaning of values at any position of the string.
     *
     * @see [serializePacket]
     * @see [Packet]
     */
    override fun parsePacket(body: String): Packet? = nullOnFail {
        if (isValidPacket(body) != true) {
            Timber.w("Body is \"body\", which is not a LocationSupport packet.")
            return@nullOnFail null
        }

        // Get fields by splitting them with [FIELD_SPLITTER].
        val payload = body.removePrefix(GEO_MMS_PREFIX)
        val payloadFields = payload.split(FIELD_SPLITTER)

        // Must be over 2 fields. (type, id)
        if (payloadFields.size < 2) {
            Timber.w("Necessary fields are missing.")
            return@nullOnFail null
        }

        // Get type number from type field of the payload.
        // It uses convert, therefore we need to catch exceptions.
        val typeNumber = try {
            with(Packet.Field.TYPE) {
                convert(payloadFields[positionInPayload])
            }
        }
        catch (e: NumberFormatException) {
            Timber.w("Parse error while getting the id of packet.")
            return@nullOnFail null
        }
        catch (e: Exception) {
            Timber.w("Unknown error occurred while getting the id of packet.")
            Timber.w(e)
            return@nullOnFail null
        }

        // Get [PacketType] from the type number.
        val type =
            findType(typeNumber)

        // If no type found, that's a problem.
        if (type == null) {
            Timber.w("Undefined type: $typeNumber")
            return@nullOnFail null
        }

        // We are going to parse fields using JSON.
        val json = JsonObject()

        // We know the type. So we know what fields are used.
        // Add pairs of field name(key) and the value to the JSON object.
        type.fields.forEach {
            val value = try {
                // Same here, it can throw something.
                it.convert(payloadFields[it.positionInPayload])
            }
            catch (e: NumberFormatException) {
                Timber.w("Parse error at payload field ${it.positionInPayload}: ${payloadFields[it.positionInPayload]}")
                return@nullOnFail null
            }
            catch (e: Exception) {
                Timber.w("Unknown error occurred while adding parsed number to json object.")
                Timber.w(e)
                return@nullOnFail null
            }

            // If successful, add the pair.
            json.addProperty(it.fieldName, value)
        }

        // Get [Packet] instance from the JSON.
        return@nullOnFail try {
            Gson().fromJson(json, Types.typeOf<Packet>())
        }
        catch (e: JsonSyntaxException) {
            Timber.w("Error while parsing json. Json syntax incorrect.")
            return@nullOnFail null
        }
        catch (e: Exception) {
            Timber.w("Unknown error occurred while parsing json.")
            Timber.w(e)
            return@nullOnFail null
        }
    }

    /**
     * Serialize [Packet] to a plain string.
     * Two fixed field: type of packet and connection id.
     *
     * @see [parsePacket]
     * @see [Packet].
     */
    override fun serializePacket(packet: Packet): String? = nullOnFail {
        val builder = StringBuilder().append(GEO_MMS_PREFIX)

        // Find the type of the packet.
        val type = Packet.PacketType.values().find { it.number == packet.type }

        if (type == null) {
            Timber.w("Wrong packet type: ${packet.type}")
            return@nullOnFail null
        }

        // Stringify each field.
        type.fields.forEach {
            val value = try {
                Reflection.readInstanceProperty<Any>(packet, it.fieldName).toString()
            }
            catch (e: Exception) {
                Timber.w("Error occurred while accessing property.")
                Timber.w(e)
                return@nullOnFail null
            }

            // If successful, add it to serialized string.
            builder.append(value)

            // Add splitter except the last one.
            if (it != type.fields.last()) {
                builder.append(FIELD_SPLITTER)
            }
        }

        return@nullOnFail builder.toString()
    }

    override fun isValidPacket(body: String): Boolean = falseOnFail {
        if (body.isBlank())                     return@falseOnFail false   /* Empty message */
        if (!body.startsWith(GEO_MMS_PREFIX))   return@falseOnFail false   /* Not a packet. */

        return@falseOnFail true
    }

    private fun findType(typeNum: Number): Packet.PacketType? = nullOnFail {
        return@nullOnFail Packet.PacketType.values().find { it.number == typeNum }
    }


    /************************************
     * CONNECTION TASK
     ************************************/

    /**
     * Only once when app started.
     * Add scheduled tasks for active connections.
     */
    private fun restoreTasks() = falseOnFail {
        scheduler.cancelAll()

        return@falseOnFail getConnections()
            ?.filter { !it.isTemporal }
            ?.map { connection ->
                if (connection.isExpired()) {
                    executeInDefaultInstance { connection.deleteFromRealm() }
                    true
                } else {
                    registerTask(connection.id)
                }
            }
            ?.takeIf { it.isNotEmpty() }
            ?.reduce { acc, b -> acc && b } ?: false
    }

    /**
     * Delete connection when expired.
     *
     * Originally this method also scheduled the connection
     * to send data for every [UPDATE_INTERVAL].
     *
     * The connection id is good to use as a task id.
     *
     * @param connection must be a realm managed object.
     */
    private fun registerTask(connectionId: Long) = falseOnFail {
        val connection = validator.validate(getConnection(connectionId, temporal = false))

        if (connection == null) {
            fail(R.string.fail_cannot_register_connection_invalid, show = true)
            return@falseOnFail false
        }

        val expireConnection = {
            // Also here, avoid using realm object.
            closeExpiredConnection(connectionId)
        }

        // Request disconnect when expired.
        scheduler.doAtTime(connection.id, connection.due, expireConnection)

        Timber.i("Connection $connection.id will be disconnected at ${DateTime(connection.due)}")

        return@falseOnFail true
    }

    /**
     * Unegister periodic update task of connection.
     *
     * @param connection must be a getRealm managed object.
     */
    private fun unregisterTask(connectionId: Long) = falseOnFail {
        scheduler.cancel(connectionId)

        Timber.i("Task unregistered: connection $connectionId.")

        return@falseOnFail true
    }

    /**
     * Send update for connection of [connectionId] for [repeat] times,
     * with [interval] of interval.
     */
    private fun scheduleSendUpdate(connectionId: Long, repeat: Long, interval: Long) {
        val sendBroadcast = {
            // This closure may be launched in a thread which is
            // not a thread the connection object is created.
            context.sendBroadcast(
                Intent(context, SendUpdateReceiver::class.java).apply {
                    putExtra(EXTRA_CONNECTION_ID, connectionId)
                }
            )
        }

        scheduler.doFor(connectionId, repeat, interval, sendBroadcast)

        scheduler
    }

    private fun closeExpiredConnection(connectionId: Long) = falseOnFail {
        val connection = validator.validate(getConnection(connectionId, temporal = false))

        if (connection == null) {
            fail(R.string.fail_close_expired_connection_invalid, show = true)
            return@falseOnFail false
        }

        // Do not validate the connection with not expired condition
        // because it will delete it and its task if it is not expired yet.
        // Instead, check isExpired here.
        if (!connection.isExpired()) {
            fail(R.string.fail_close_connection_not_expired, show = true)
            return@falseOnFail false
        }

        unregisterTask(connectionId)

        Timber.i("Closed expired connection of id ${connection.id}")

        executeInDefaultInstance {
            connection.deleteFromRealm()
        }

        return@falseOnFail true
    }

    /************************************
     * REALM
     ************************************/

    private fun getRealm(): Realm {
        return Realm.getDefaultInstance()
    }

    private fun executeInDefaultInstance(close: Boolean = true, transaction: (Realm) -> Unit) {
        val realm = getRealm()

        realm.executeTransaction(transaction)

        if (close && !realm.isClosed) {
            realm.close()
        }
    }


    /************************************
     * UTILITY
     ************************************/

    override fun fail(@StringRes message: Int, vararg formatArgs: Any?, show: Boolean) {
        setFailure(Failable.Failure(context.getString(message, *formatArgs), show))
    }


    /************************************
     * VALIDATOR
     ************************************/

    /**
     * Validate = check + correct
     *
     * Insure location connections and requests are valid.
     * If found something not valid, perform correction(e.g. delete)
     */
    inner class Validator {
        private val validation = HashMap<Class<*>, Validation<RealmObject>>()

        /**
         * Validation declaration here.
         */
        init {
            validation[Connection::class.java] = Validation<Connection>(
                checker = {
                    if (!it.isValid) {
                        Timber.i("Connection is not valid(realm)")
                        return@Validation false
                    }
                    else if (it.id == 0L) {
                        Timber.i("Connection id is zero.")
                        return@Validation false
                    }
                    else if (it.recipient == null) {
                        Timber.i("Recipient of connection is null.")
                        return@Validation false
                    }
                    else {
                        Timber.i("Connection ${it.id} is valid :)")
                        return@Validation true
                    }
                },
                corrector = { connection ->
                    if (connection.isValid) {
                        // The correction contains removing task.
                        // This action is safe because this method does not call validate().
                        unregisterTask(connection.id)
                        Timber.i("Correct wrong connection: unregister task.")
                    }
                    executeInDefaultInstance {
                        connection.deleteFromRealm()
                        Timber.i("Correct wrong connection: remove from realm.")
                    }
                    null
                }
            )

            validation[ConnectionRequest::class.java] = Validation<ConnectionRequest>(
                checker = {
                    if (!it.isValid) {
                        Timber.i("Request is not valid(realm).")
                        return@Validation false
                    }
                    else if (it.connectionId == 0L) {
                        Timber.i("Connection id of request is zero.")
                        return@Validation false
                    }
                    else if (it.recipient == null) {
                        Timber.i("Recipient of request is null.")
                        return@Validation false
                    }
                    else {
                        return@Validation true
                    }
                },
                corrector = { realmObject ->
                    executeInDefaultInstance {
                        realmObject.deleteFromRealm()
                        Timber.i("Correct wrong request: remove from realm.")
                    }
                    null
                }
            )
        }

        /**
         * Check if [locationObject] is valid.
         * Correct it if not valid.
         *
         * @param locationObject The realm object to validate.
         * @return The [locationObject] if valid, or null.
         */
        fun <T : RealmObject> validate(locationObject: T?, additionalPredicate: (T) -> Boolean = { true }): T? {
            return if (isValid(locationObject, additionalPredicate)) {
                locationObject
            } else {
                performCorrection(locationObject)
            }
        }

        fun validateAll() {
            getIncomingRequests()?.forEach { validate(it) }
            getIncomingRequests()?.forEach { validate(it) }
            getOutgoingRequests()?.forEach { validate(it) }
        }

        /**
         * Check.
         * Null check here.
         */
        private fun <T: RealmObject> isValid(locationObject: T?, additionalPredicate: (T) -> Boolean = { true }): Boolean {
            if (locationObject == null) {
                Timber.i("Location object is null :(")
                return false
            }

            val foundChecker = getValidation(locationObject)?.checker
            if (foundChecker == null) {
                fail(R.string.fail_validator_not_found, locationObject::class.java.name, show = true)
                return false
            }

            return foundChecker(locationObject) && additionalPredicate(locationObject)
        }

        /**
         * Correct.
         * Null check here.
         */
        private fun <T: RealmObject> performCorrection(locationObject: T?): T? {
            locationObject ?: return null

            Timber.i("Performing correction for invalid object $locationObject.")

            val foundCorrector = getValidation(locationObject)?.corrector
            if (foundCorrector == null) {
                fail(R.string.fail_validator_not_found, locationObject::class.java.name, show = true)
                return null
            }

            return foundCorrector(locationObject)
        }

        @Suppress("UNCHECKED_CAST")
        private fun <T: RealmObject> getValidation(locationObject: T): Validation<T>? {
            // Managed realm object is replaced by a proxy class instance.
            // We need to get an un-managed copy of realm object
            // to get the model class packageName.
            return validation[getRealm().copyFromRealm(locationObject)::class.java] as? Validation<T>
        }
    }

    data class Validation<out T>(
        val checker: (@UnsafeVariance T) -> Boolean,
        val corrector: (@UnsafeVariance T) -> T?
    )

    companion object {
        const val GEO_MMS_PREFIX = "[GEOMMS]"
        const val FIELD_SPLITTER = ":"

        const val UPDATE_INTERVAL = 1 * 60 * 1000L // 1 min
    }
}