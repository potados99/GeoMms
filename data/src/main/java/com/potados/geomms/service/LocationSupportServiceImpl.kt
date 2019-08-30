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
import com.potados.geomms.extension.nullOnFail
import com.potados.geomms.extension.unitOnFail
import com.potados.geomms.manager.KeyManager
import com.potados.geomms.model.Connection
import com.potados.geomms.model.ConnectionRequest
import com.potados.geomms.model.Packet
import com.potados.geomms.model.Recipient
import com.potados.geomms.receiver.SendUpdateReceiver
import com.potados.geomms.receiver.SendUpdateReceiver.Companion.EXTRA_CONNECTION_ID
import com.potados.geomms.repository.ConversationRepository
import com.potados.geomms.repository.LocationRepository
import com.potados.geomms.util.*
import io.realm.Realm
import io.realm.RealmObject
import io.realm.RealmResults
import io.realm.Sort
import timber.log.Timber

/**
 * FOREWORD: This is not android.app.Service.
 *
 * This class implements the main feature of this app:
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
 *
 */
class LocationSupportServiceImpl(
    private val context: Context,
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
        restoreTasks()
        startLocationUpdates()

        started = true

        Timber.i("Service started.")
    }

    override fun isIdle(): Boolean {
        return getConnections()?.isEmpty() == true
                && getIncomingRequests()?.isEmpty() == true
                && getOutgoingRequests()?.isEmpty() == true
    }

    override fun clearAll() = unitOnFail {
        disconnectAll()
        refuseAll()
        cancelAll()
    }

    override fun disconnectAll() = unitOnFail {
        getConnections()?.filter { !it.isTemporal }?.forEach {
            requestDisconnect(it.id)
        }
    }

    override fun refuseAll() = unitOnFail {
        getIncomingRequests()?.forEach {
            refuseConnectionRequest(it)
        }
    }

    override fun cancelAll() = unitOnFail {
        getOutgoingRequests()?.forEach {
            cancelConnectionRequest(it)
        }
    }


    /************************************
     * INTERNAL STATE CONTROL
     ************************************/

    private fun startLocationUpdates() = unitOnFail {
        locationRepo.startLocationUpdates()
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
    override fun getConnection(id: Long, temporal: Boolean): Connection? = nullOnFail {
        return@nullOnFail getRealm()
            .where(Connection::class.java)
            .equalTo("id", id)
            .equalTo("isTemporal", temporal)
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


    /************************************
     * TAKE & HANDLE ACTION
     ************************************/

    override fun requestNewConnection(address: String, duration: Long) = unitOnFail {
        val recipient = getRecipient(address) ?: return@unitOnFail

        val request = ConnectionRequest(
            connectionId = keyManager.randomId(99999),
            recipient = recipient,
            isInbound = false, // outbound
            date = System.currentTimeMillis(), // now
            duration = duration
        )

        sendPacket(address, Packet.ofRequestingNewConnection(request))

        // Add this not-yet accepted connection to getRealm.
        val temporalConnection = Connection.fromAcceptedRequest(request).apply { isTemporal = true }

        executeInDefaultInstance {
            it.insertOrUpdate(request)
            it.insertOrUpdate(temporalConnection)
        }

        Timber.i("Requested connection to ${recipient.getDisplayName()} with id ${request.connectionId}.")
    }
    override fun beRequestedNewConnection(packet: Packet) = unitOnFail {
        // requests can be duplicated but connections cannot.
        getConnection(packet.connectionId, temporal = false)?.let {
            fail(R.string.fail_ignore_illegal_request, false)
            return@unitOnFail
        }

        val request = requestFromInboundPacket(packet)

        if (request == null) {
            fail(R.string.fail_cannot_obtain_request, show = true)
            return@unitOnFail
        }

        executeInDefaultInstance { it.insertOrUpdate(request) }

        Timber.i("New incoming request from ${request.recipient?.getDisplayName()} with id ${request.connectionId}.")
    }

    override fun acceptConnectionRequest(request: ConnectionRequest) = unitOnFail {
        val validated = validator.validate(request) {
            it.isInbound && getConnection(it.connectionId, false) == null
        }

        // If is not inbound or connection already exists.
        if (validated == null) {
            fail(R.string.fail_cannot_accept_request_invalid, show = true)
            return@unitOnFail
        }

        // Notify accepted.
        request.recipient?.let {
            sendPacket(it.address, Packet.ofAcceptingRequest(request))
        }

        val connection = Connection.fromAcceptedRequest(request)

        // add connection, delete request
        executeInDefaultInstance { realm ->
            realm.copyToRealmOrUpdate(connection).let(::registerTask)
            request.deleteFromRealm()
        }

        // start sending updates

        Timber.i("Accept -> New connection(${connection.id}) established with ${connection.id}.")
    }
    override fun beAcceptedConnectionRequest(packet: Packet) = unitOnFail {
        val request = validator.validate(getRequest(packet.connectionId, inbound = false)) {
            getConnection(it.connectionId, false) == null
        }

        if (request == null) {
            fail(R.string.fail_ignore_wrong_accept, show = false)
            return@unitOnFail
        }

        // if there is a temporal connection already added, it will be updated.
        val connection = Connection.fromAcceptedRequest(request)

        executeInDefaultInstance {
            it.copyToRealmOrUpdate(connection).let(::registerTask)
        }

        Timber.i("Accepted -> New connection(${connection.id}) established with ${connection.id}.")
    }

    override fun refuseConnectionRequest(request: ConnectionRequest) = unitOnFail {
        val validated = validator.validate(request) {
            it.isInbound && getConnection(it.connectionId, false) == null
        }

        // If is not inbound or connection already exists.
        if (validated == null) {
            fail(R.string.fail_cannot_refuse_request_invalid, show = true)
            return@unitOnFail
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
    }
    override fun beRefusedConnectionRequest(packet: Packet) = unitOnFail {
        val request = validator.validate(getRequest(packet.connectionId, inbound = false))  {
            getConnection(it.connectionId, false) == null
        }

        if (request == null) {
            fail(R.string.fail_ignore_wrong_refuse, show = false)
            return@unitOnFail
        }

        Timber.i("Request of id ${request.connectionId} to ${request.recipient?.getDisplayName()} is refused.")

        executeInDefaultInstance {
            request.deleteFromRealm()
        }
    }

    override fun cancelConnectionRequest(request: ConnectionRequest) = unitOnFail {
        val temporalConnection = validator.validate(getConnection(request.connectionId, temporal = true))

        val validated = validator.validate(request) {
            !it.isInbound && getConnection(it.connectionId, false) == null
        }

        if (validated == null) {
            fail(R.string.fail_cannot_cancel_request_invalid, show = true)
            return@unitOnFail
        }

        // Notify canceled.
        request.recipient?.let {
            sendPacket(it.address, Packet.ofCancelingRequest(request))
        }

        Timber.i("Cancel request of id ${request.connectionId} to ${request.recipient?.getDisplayName()}")

        executeInDefaultInstance {
            temporalConnection?.deleteFromRealm()
            request.deleteFromRealm()
        }
    }
    override fun beCanceledConnectionRequest(packet: Packet) = unitOnFail {
        val request = validator.validate(getRequest(packet.connectionId, inbound = true))  {
            getConnection(it.connectionId, false) == null
        }

        if (request == null) {
            fail(R.string.fail_ignore_wrong_cancel, show = false)
            return@unitOnFail
        }

        Timber.i("Request of id ${request.connectionId} from ${request.recipient?.getDisplayName()} is canceled by the sender.")

        executeInDefaultInstance { request.deleteFromRealm() }
    }

    override fun sendUpdate(connectionId: Long) = unitOnFail {
        val connection = validator.validate(getConnection(connectionId, temporal = false))

        if (connection == null) {
            fail(R.string.fail_cannot_send_update_invalid_connection, show = true)
            return@unitOnFail
        }

        val packet = Packet.ofSendingData(connection, locationRepo.getCurrentLocation() ?: return@unitOnFail)

        sendPacket(connection.recipient?.address ?: return@unitOnFail, packet)

        executeInDefaultInstance {
            connection.lastSent = System.currentTimeMillis()
        }

        Timber.i("Sent update.")
    }
    override fun beSentUpdate(packet: Packet) = unitOnFail {
        val connection = validator.validate(getConnection(packet.connectionId, false))

        if (connection == null) {
            fail(context.getString(R.string.fail_ignore_wrong_data), show = false)
            return@unitOnFail
        }

        executeInDefaultInstance {
            connection.lastUpdate = System.currentTimeMillis()
            connection.latitude = packet.latitude
            connection.longitude = packet.longitude
        }

        Timber.i("Received update of connection ${connection.id} from ${connection.recipient?.getDisplayName()}.")
    }

    override fun requestUpdate(connectionId: Long) = unitOnFail {
        val connection = getConnection(connectionId, temporal = false)

        if (connection == null) {
            fail(R.string.fail_cannot_request_update_invalid_connection, show = true)
            return@unitOnFail
        }

        val packet = Packet.ofRequestingUpdate(connection)

        sendPacket(connection.recipient?.address ?: return@unitOnFail, packet)

        Timber.i("Requested update of ${connection.id} to ${connection.recipient?.getDisplayName()}.")
    }
    override fun beRequestedUpdate(packet: Packet) = unitOnFail {
        val connection = validator.validate(getConnection(packet.connectionId, false))

        if (connection == null) {
            // This could be an illegal try.
            // Think how to handle it. (e.g. notify user with warning)
            fail(R.string.fail_ignore_wrong_update_request, show = false)
            return@unitOnFail
        }

        sendUpdate(connection.id)

        Timber.i("Sent update of connection ${connection.id} in response of ${connection.recipient?.getDisplayName()}'s request.")
    }

    override fun requestDisconnect(connectionId: Long) = unitOnFail {
        val connection = validator.validate(getConnection(connectionId, temporal = false))

        if (connection == null) {
            fail(R.string.fail_cannot_request_disconnect_invalid_connection, show = true)
            return@unitOnFail
        }

        // Stop sending updates.
        unregisterTask(connection)

        val packet = Packet.ofRequestingDisconnect(connection)

        sendPacket(connection.recipient?.address ?: return@unitOnFail, packet)

        Timber.i("Requested disconnect of connection ${connection.id} to ${connection.recipient?.getDisplayName()}")

        executeInDefaultInstance {
            connection.deleteFromRealm()
        }
    }
    override fun beRequestedDisconnect(packet: Packet) = unitOnFail {
        val connection = validator.validate(getConnection(packet.connectionId, false))

        if (connection == null) {
            fail(R.string.fail_ignore_wrong_disconnect, show = false)
            return@unitOnFail
        }

        sendPacket(connection.recipient?.address ?: return@unitOnFail, packet)

        Timber.i("Disconnect and delete connection of id ${connection.id} in response of ${connection.recipient?.getDisplayName()}'s request.")

        executeInDefaultInstance {
            connection.deleteFromRealm()
        }
    }


    /************************************
     * PACKET PROCESS
     ************************************/

    override fun sendPacket(address: String, packet: Packet) = unitOnFail {
        val serialized = serializePacket(packet) ?: return@unitOnFail

        SmsManager.getDefault().sendTextMessage(
            address,
            null,
            serialized,
            null,
            null
        )

        Timber.i("Sent packet: \"$serialized\"")
    }
    override fun receivePacket(address: String, body: String) = unitOnFail {
        val packet = parsePacket(body)?.apply {
            this.address = address
            this.isInbound = true
        } ?: return@unitOnFail

        Timber.i("Received packet is ${findType(packet.type).toString()}")

        when (packet.type) {

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
        }

        Timber.i("Successfully handled packet.")
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

    override fun isValidPacket(body: String): Boolean? = nullOnFail {
        if (body.isBlank())                     return@nullOnFail false   /* Empty message */
        if (!body.startsWith(GEO_MMS_PREFIX))   return@nullOnFail false   /* Not a packet. */

        return@nullOnFail true
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
    private fun restoreTasks() = unitOnFail {
        scheduler.cancelAll()
        executeInDefaultInstance {
            getConnections()?.filter { !it.isTemporal }?.forEach {
                if (it.isExpired()) {
                    it.deleteFromRealm()
                }
                else {
                    registerTask(it)
                }
            }
        }
    }

    /**
     * Register periodic update task of connection.
     * The connection id is good to use as a task id.
     *
     * @param connection must be a realm managed object.
     */
    private fun registerTask(connection: Connection) = unitOnFail {
        val sendBroadcast = {
            context.sendBroadcast(
                Intent(context, SendUpdateReceiver::class.java).apply {
                    putExtra(EXTRA_CONNECTION_ID, connection.id)
                }
            )
        }
        val expireConnection = {
            closeExpiredConnection(connection)
        }

        // Send update every [UPDATE_INTERVAL].
        scheduler.doOnEvery(connection.id, UPDATE_INTERVAL, sendBroadcast)

        // Request disconnect when expired.
        scheduler.doAtTime(connection.id, connection.due, expireConnection)

        Timber.i("Task registered: connection ${connection.id}, for every ${Duration(UPDATE_INTERVAL).toShortenString()}")
        Timber.i("Will be disconnected at ${DateTime(connection.due)}")
    }

    /**
     * Unegister periodic update task of connection.
     *
     * @param connection must be a getRealm managed object.
     */
    private fun unregisterTask(connection: Connection) = unitOnFail {
        scheduler.cancel(connection.id)

        Timber.i("Task unregistered: connection ${connection.id}.")
    }

    private fun closeExpiredConnection(connection: Connection) = unitOnFail {
        if (!connection.isExpired()) {
            Timber.w("Connection is not expired yet!")
        }

        unregisterTask(connection)

        Timber.i("Closed expired connection of id ${connection.id}")

        executeInDefaultInstance {
            connection.deleteFromRealm()
        }
    }

    /************************************
     * REALM
     ************************************/

    private fun getRealm(): Realm {
        return Realm.getDefaultInstance()
    }

    private fun executeInDefaultInstance(close: Boolean = true, transaction: (Realm) -> Unit) {
        val realm = getRealm()

        realm.beginTransaction()

        try {
            transaction(realm)
            realm.commitTransaction()
            if (close) {
                realm.close()
            }
        } catch (e: Exception) {
            if (realm.isInTransaction) {
                realm.cancelTransaction()
                Timber.i("Canceled transaction.")
            }
            fail(R.string.fail_transaction, e.message, show = true)
        }
    }


    /************************************
     * UTILITY
     ************************************/

    private fun fail(message: String, show: Boolean = false) {
        setFailure(Failable.Failure(message, show))
    }

    private fun fail(@StringRes message: Int, vararg formatArgs: Any?, show: Boolean = false) {
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
                    return@Validation it.id != 0L && it.recipient != null && !it.isExpired()
                },
                corrector = { realmObject ->
                    executeInDefaultInstance { realmObject.deleteFromRealm() }
                    null
                }
            )

            validation[ConnectionRequest::class.java] = Validation<ConnectionRequest>(
                checker = {
                    return@Validation it.connectionId != 0L && it.recipient != null
                },
                corrector = { realmObject ->
                    executeInDefaultInstance { realmObject.deleteFromRealm() }
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
            locationObject ?: return false

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

            val foundCorrector = getValidation(locationObject)?.corrector
            if (foundCorrector == null) {
                fail("Validator not found for type ${locationObject::class.java.name}.", true)
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