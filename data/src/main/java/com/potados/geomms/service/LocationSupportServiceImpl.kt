package com.potados.geomms.service

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.telephony.SmsManager
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.google.gson.JsonSyntaxException
import com.potados.geomms.base.Failable
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
import java.lang.reflect.Type
import kotlin.reflect.KClass
import kotlin.reflect.typeOf

class LocationSupportServiceImpl(
    private val context: Context,
    private val conversationRepo: ConversationRepository,
    private val locationRepo: LocationRepository,
    private val scheduler: Scheduler,
    private val keyManager: KeyManager
) : LocationSupportService() {

    private var started = false

    private val validator = Validator()

    override fun start() {
        if (started) {
            Timber.i("Already started!")

            return
        }

        super.start()

        validator.validateAll()
        restoreTasks()
        startLocationUpdates()

        started = true

        Timber.i("Service started.")
    }

    override fun disconnectAll() {
        getConnections()?.forEach {
            requestDisconnect(it.id)
        }
    }

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
            setFailure(Failable.Failure("Failed to retrieve recipient of address $address.", true))
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
    }
    override fun beRequestedNewConnection(packet: Packet) = unitOnFail {
        // requests can be duplicated but connections cannot.
        getConnectionNoCheck(packet.connectionId)?.let {
            setFailure(Failable.Failure("Ignore request of already established connection or sent request.", true))
            return@unitOnFail
        }

        val request = requestFromInboundPacket(packet)

        if (request == null) {
            setFailure(Failable.Failure("Cannot obtain request object from packet.", true))
            return@unitOnFail
        }

        Timber.i("New incoming request: ${request.connectionId}")

        executeInDefaultInstance { it.insertOrUpdate(request) }
    }

    override fun acceptConnectionRequest(request: ConnectionRequest) = unitOnFail {
        val validated = validator.validate(request) {
            it.isInbound && getConnection(it.connectionId, false) == null
        }

        if (validated == null) {
            setFailure(Failable.Failure("Request is invalid.", true))
            return@unitOnFail
        }

        // tell it to YOU
        request.recipient?.let {
            sendPacket(it.address, Packet.ofAcceptingRequest(request))
        }

        // add connection, delete request
        val connection = Connection.fromAcceptedRequest(request)

        executeInDefaultInstance { realm ->
            realm.insertOrUpdate(connection)
            request.deleteFromRealm()
        }

        Timber.i("New connection: ${connection.id}")

        // start sending updates
        registerTask(connection)
    }
    override fun beAcceptedConnectionRequest(packet: Packet) = unitOnFail {
        val request = validator.validate(getRequest(packet.connectionId, inbound = false))

        if (request == null) {
            setFailure(Failable.Failure("Request is accepted but cannot find the outbound request from DB.", true))
            return@unitOnFail
        }

        // add connection.
        // if there are isTemporal connection already added,
        // it will be updated.
        val connection = Connection.fromAcceptedRequest(request)
        executeInDefaultInstance { it.insertOrUpdate(connection) }

        Timber.i("New connection: ${connection.id}")

        // start sending updates.
        registerTask(connection)
    }

    override fun refuseConnectionRequest(request: ConnectionRequest) = unitOnFail {
        val validated = validator.validate(request) {
            it.isInbound && getConnection(it.connectionId, false) == null
        }
        if (validated == null) {
            setFailure(Failable.Failure("Request is invalid.", true))
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
    }
    override fun beRefusedConnectionRequest(packet: Packet) = unitOnFail {
        val request = getRequest(packet.connectionId, inbound = false)

        if (request == null) {
            setFailure(Failable.Failure("Ignore refuse of non-existing request.", true))
            return@unitOnFail
        }

        executeInDefaultInstance {
            request.deleteFromRealm()
        }
    }

    override fun cancelConnectionRequest(request: ConnectionRequest) = unitOnFail {
        val validated = validator.validate(request) {
            !it.isInbound && getConnection(it.connectionId, false) == null
        }
        if (validated == null) {
            setFailure(Failable.Failure("Request is invalid.", true))
            return@unitOnFail
        }

        // let you know
        request.recipient?.let {
            sendPacket(it.address, Packet.ofCancelingRequest(request))
        }

        // find temporal connection
        val temporalConnection = validator.validate(getConnection(request.connectionId, temporal = true))

        // delete
        executeInDefaultInstance {
            temporalConnection?.deleteFromRealm()
            request.deleteFromRealm()
        }
    }
    override fun beCanceledConnectionRequest(packet: Packet) = unitOnFail {
        val request = validator.validate(getRequest(packet.connectionId, inbound = true))

        if (request == null) {
            val recipient = getRecipient(packet.address)
            setFailure(Failable.Failure("${recipient?.getDisplayName()} want to cancel request but nothing to cancel.", true))
            return@unitOnFail
        }

        executeInDefaultInstance { request.deleteFromRealm() }
    }

    override fun sendUpdate(connectionId: Long) = unitOnFail {
        val connection = validator.validate(getConnection(connectionId, temporal = false))
            ?: return@unitOnFail

        val packet = Packet.ofSendingData(connection, locationRepo.getCurrentLocation() ?: return@unitOnFail)

        sendPacket(connection.recipient?.address ?: return@unitOnFail, packet)
    }
    override fun beSentUpdate(packet: Packet) = unitOnFail {
        val connection = validator.validate(getConnection(packet.connectionId, false))
            ?: return@unitOnFail

        executeInDefaultInstance {
            connection.lastUpdate = System.currentTimeMillis()
            connection.latitude = packet.latitude
            connection.longitude = packet.longitude
        }
    }

    override fun requestUpdate(connectionId: Long) = unitOnFail {
        val connection = getConnection(connectionId, temporal = false) ?: return@unitOnFail

        val packet = Packet.ofRequestingUpdate(connection)

        sendPacket(connection.recipient?.address ?: return@unitOnFail, packet)

        Timber.i("Requested update")
    }
    override fun beRequestedUpdate(packet: Packet) = unitOnFail {
        val connection = validator.validate(getConnection(packet.connectionId, false))
            ?: return@unitOnFail

        sendUpdate(connection.id)
    }

    override fun requestDisconnect(connectionId: Long) = unitOnFail {
        val connection = validator.validate(getConnection(connectionId, temporal = false))
            ?: return@unitOnFail

        val packet = Packet.ofRequestingDisconnect(connection)

        // stop sending
        unregisterTask(connection)

        // notify
        sendPacket(connection.recipient?.address ?: return@unitOnFail, packet)

        // delete
        executeInDefaultInstance {
            connection.deleteFromRealm()
        }

        Timber.i("Requested disconnect")
    }
    override fun beRequestedDisconnect(packet: Packet) = unitOnFail {
        val connection = validator.validate(getConnection(packet.connectionId, false))
            ?: return@unitOnFail

        sendPacket(connection.recipient?.address ?: return@unitOnFail, packet)

        executeInDefaultInstance {
            connection.deleteFromRealm()
        }
    }


    override fun sendPacket(address: String, packet: Packet) = unitOnFail {
        val serialized = serializePacket(packet) ?: return@unitOnFail

        SmsManager.getDefault().sendTextMessage(
            address,
            null,
            serialized,
            null,
            null
        )

        Timber.i("sent packet: \"$serialized\"")
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
        /**
         * 예외처리
         */
        if (isValidPacket(body) != true) {
            Timber.w("not a LocationSupport packet.")
            return@nullOnFail null
        }

        /**
         * 페이로드의 필드들 가져오기.
         */
        val payload = body.removePrefix(GEO_MMS_PREFIX)
        val payloadFields = payload.split(FIELD_SPLITTER)

        /**
         * 고정 필드가 type과 id로 두 개인데, 이보다 적으면 잘못된 패킷으로 간주.
         */
        if (payloadFields.size < 2) {
            Timber.w("necessary fields are missing.")
            return@nullOnFail null
        }

        /**
         * 페이로드 중 고정 부분의 첫번째 필드인 type 숫자 값을 가져옵니다.
         */
        val typeNumber = with(Packet.Field.TYPE) {
            convert(payloadFields[positionInPayload])
        }

        /**
         * 해당 typeNumber에 해당하는 PacketType을 가져옵니다.
         */
        val type =
            findType(typeNumber)

        /**
         * 없으면 잘못된 패킷.
         */
        if (type == null) {
            Timber.w("undefined type: $typeNumber")
            return@nullOnFail null
        }

        /**
         * json으로 만들어서 담을겁니다.
         */
        val json = JsonObject()

        /**
         * 가져온 타입에 해당하는 필드들을 가지고 와서,
         * {필드 이름}과 {페이로드에서 가져온 그 필드의 값} 쌍을 json 객체에 추가해줍니다.
         */
        type.fields.forEach {
            val value = try {
                /**
                 * Convert를 굳이 여기서 해주는 이유는, 숫자 스트링이 무결한지 여기에서 확인하기 위함입니다.
                 * Gson이 검사하게 해도 되는데 그냥 직접 하고 싶었습니다.
                 */
                it.convert(payloadFields[it.positionInPayload])
            }
            catch (e: Exception) {
                when (e) {
                    /** toDouble이나 toLong에서 문제가 생긴 경우 */
                    is NumberFormatException -> {
                        Timber.w("parse error at payload field ${it.positionInPayload}: ${payloadFields[it.positionInPayload]}")
                    }
                    else -> { /* 그렇지 않은 경우 */
                        Timber.w("unknown error occurred while adding parsed number to json object.")
                    }
                }

                return@nullOnFail null
            }

            json.addProperty(it.fieldName, value)
        }

        /**
         * Packet 객체 확보.
         */
        return@nullOnFail try {
            Gson().fromJson(json, Types.typeOf<Packet>())
        }
        catch (e: Exception) {
            when (e) {
                is JsonSyntaxException -> {
                    Timber.w("error while parsing json. json syntax incorrect.")
                }
                else -> {
                    Timber.w("unknown error occurred while parsing json.")
                }
            }
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

        val type = Packet.PacketType.values().find { it.number == packet.type }

        if (type == null) {
            Timber.w("wrong packet type: ${packet.type}")
            return@nullOnFail null
        }

        type.fields.forEach {
            val value = try {
                Reflection.readInstanceProperty<Any>(packet, it.fieldName).toString()
            }
            catch (e: Exception) {
                Timber.w("error occurred while accessing property.")
                return@nullOnFail null
            }

            builder.append(value)

            if (it != type.fields.last()) {
                builder.append(FIELD_SPLITTER)
            }
        }

        return@nullOnFail builder.toString()
    }

    override fun isValidPacket(body: String): Boolean? = nullOnFail {
        if (body.isBlank())                     return@nullOnFail false   /* 비어있는 메시지 */
        if (!body.startsWith(GEO_MMS_PREFIX))   return@nullOnFail false   /* 무관한 메시지 */

        return@nullOnFail true
    }

    private fun findType(typeNum: Number): Packet.PacketType? = nullOnFail {
        return@nullOnFail Packet.PacketType.values().find { it.number == typeNum }
    }

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

    private fun startLocationUpdates() = unitOnFail {
        locationRepo.startLocationUpdates()
    }

    /**
     * Register periodic update task of connection.
     * The connection id is good to use as a task id.
     *
     * TODO: UPDATE_INTERVAL hardcoded. fix it.
     *
     * @param connection must be a getRealm managed object.
     */
    private fun registerTask(connection: Connection) = unitOnFail {
        scheduler.doOnEvery(connection.id, UPDATE_INTERVAL) {
            context.sendBroadcast(
                Intent(context, SendUpdateReceiver::class.java)
                    .apply { putExtra(EXTRA_CONNECTION_ID, connection.id) }
            )
        }
        scheduler.doAtTime(connection.id, connection.due) {
            requestDisconnect(connection.id)
        }

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


    /**
     * Construct a request from an inbound request packet.
     * @param
     */
    private fun requestFromInboundPacket(packet: Packet): ConnectionRequest? = nullOnFail {
        if (!packet.isInbound) {
            setFailure(Failable.Failure("The packet must be inbound.", true))
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

    private fun getRealm(): Realm {
        return Realm.getDefaultInstance()
    }

    private fun executeInDefaultInstance(transaction: (Realm) -> Unit) {
        val realm = getRealm()

        realm.beginTransaction()

        try {
            transaction(realm)
            realm.commitTransaction()
        } catch (e: Exception) {
            if (realm.isInTransaction) {
                realm.cancelTransaction()
            }
            setFailure(Failable.Failure(e.message ?: "Transaction error occurred.", true))
        }
    }

    private fun getConnectionNoCheck(id: Long): Connection? = nullOnFail {
        return@nullOnFail getRealm()
            .where(Connection::class.java)
            .equalTo("id", id)
            .findFirst()
    }

    /**
     * Remove periodic task and delete from realm.
     * Not notify to YOU.
     */
    private fun deleteConnection(connection: Connection) = unitOnFail {
        unregisterTask(connection)
        executeInDefaultInstance { connection.deleteFromRealm() }
    }

    private fun fail(message: String, show: Boolean = false) {
        setFailure(Failable.Failure(message, show))
    }


    /**
     * Validate = check + correct
     *
     * Insure location connections and requests are valid.
     * If found something not valid, perform correction(e.g. delete)
     */
    inner class Validator {
        private val validation = HashMap<Class<*>, Validation<RealmObject>>()

        init {
            Timber.i("Connection::class.java.name becomes ${Connection::class.java.name}")
            validation[Connection::class.java] = Validation<Connection>(
                checker = {
                    return@Validation it.id != 0L && it.recipient != null
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
                fail("Validator not found for type ${locationObject::class.java.name}.", true)
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
            // to get the model class name.
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