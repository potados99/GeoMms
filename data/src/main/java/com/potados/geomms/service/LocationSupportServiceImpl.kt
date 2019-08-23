package com.potados.geomms.service

import android.content.Context
import android.content.Intent
import android.telephony.SmsManager
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.google.gson.JsonSyntaxException
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
import io.realm.RealmResults
import io.realm.Sort
import timber.log.Timber

class LocationSupportServiceImpl(
    private val context: Context,
    private val conversationRepo: ConversationRepository,
    private val locationRepo: LocationRepository,
    private val scheduler: Scheduler,
    private val keyManager: KeyManager
) : LocationSupportService {

    private var started = false

    override fun start() {
        if (started) return
        restoreTasks()
        started = true
    }

    override fun getConnections(): RealmResults<Connection> {
        return Realm.getDefaultInstance().where(Connection::class.java)
            .sort("date", Sort.ASCENDING)
            .findAll()
    }

    override fun getConnection(id: Long): Connection? {
        val connection = Realm.getDefaultInstance().where(Connection::class.java)
            .equalTo("id", id)
            .findFirst()

        if (connection == null) {
            Timber.w("Connection of id $id does not exist.")
        }

        return connection
    }

    /**
     * @return Recipient of conversation created from [address].
     * @throws RuntimeException when failed to get or create conversation.
     */
    private fun getRecipient(address: String): Recipient {
        return conversationRepo.getOrCreateConversation(address)?.recipients?.get(0)
            ?: throw RuntimeException("conversation of address $address must exist.")
    }


    override fun getIncomingRequests(): RealmResults<ConnectionRequest> {
        return  Realm.getDefaultInstance().where(ConnectionRequest::class.java)
            .sort("date", Sort.ASCENDING)
            .equalTo("isInbound", true)
            .findAll()
    }

    override fun getOutgoingRequests(): RealmResults<ConnectionRequest> {
        return  Realm.getDefaultInstance().where(ConnectionRequest::class.java)
            .sort("date", Sort.ASCENDING)
            .equalTo("isInbound", false)
            .findAll()
    }


    override fun requestNewConnection(address: String, duration: Long) {
        val recipient = getRecipient(address)

        val request = ConnectionRequest(
            connectionId = keyManager.randomId(99999),
            recipient = recipient,
            isInbound = false,
            date = System.currentTimeMillis(),
            duration = duration
        )

        sendPacket(address, Packet.ofRequestingNewConnection(request))

        Realm.getDefaultInstance().executeTransaction { it.insertOrUpdate(request) }
    }
    override fun beRequestedNewConnection(packet: Packet) {
        // prevent double accepting and creating connection
        val found = Realm.getDefaultInstance()
            .where(Connection::class.java)
            .equalTo("id", packet.connectionId)
            .findFirst()

        if (found != null) return

        val request = ConnectionRequest(
            connectionId = packet.connectionId,
            recipient = getRecipient(packet.address),
            isInbound = true,
            date = packet.date,
            duration = packet.duration
        )

        Timber.i("New incoming request: ${packet.connectionId}")

        Realm.getDefaultInstance().executeTransaction { it.insertOrUpdate(request) }
    }

    /**
     * @throws IllegalAccessError when [request] is not inbound
     * @throws IllegalArgumentException when [request] has no recipients
     */
    override fun acceptConnectionRequest(request: ConnectionRequest) {
        if (!request.isInbound) throw IllegalAccessError("Cannot accept request not heading to isInbound.")
        if (request.recipient == null) throw IllegalArgumentException("Request without recipient is impossible.")

        val realm = Realm.getDefaultInstance()

        // prevent double accepting and creating connection
        val found =  Realm.getDefaultInstance()
            .where(Connection::class.java)
            .equalTo("id", request.connectionId)
            .findFirst()

        if (found != null) return

        // tell it to YOU
        request.recipient?.let {
            sendPacket(it.address, Packet.ofAcceptingRequest(request))
        }

        // add connection, delete request
        val connection = Connection.fromAcceptedRequest(request)

        realm.executeTransaction { realm ->
            realm.insertOrUpdate(connection)
            request.deleteFromRealm()
        }

        Timber.i("New connection: ${connection.id}")

        // start sending updates
        registerTask(connection)

        realm.close()
    }
    override fun beAcceptedConnectionRequest(packet: Packet) {
        val realm = Realm.getDefaultInstance()

        realm.where(ConnectionRequest::class.java)
            .equalTo("connectionId", packet.connectionId)
            .equalTo("isInbound", false) // outbound request accepted
            .findFirst()
            ?.let { request ->
                // add connection
                val connection = Connection.fromAcceptedRequest(request)
                realm.executeTransaction { it.insertOrUpdate(connection) }

                Timber.i("New connection: ${connection.id}")

                // start sending updates
                registerTask(connection)
            }
            ?: Timber.w("cannot find corresponding pending request for ACCEPT_CONNECT.")

        realm.close()
    }

    /**
     * @throws IllegalAccessError when [request] is not inbound
     * @throws IllegalArgumentException when [request] has no recipients
     */
    override fun refuseConnectionRequest(request: ConnectionRequest) {
        if (!request.isInbound) throw IllegalAccessError("Cannot refuse request not heading to isInbound.")
        if (request.recipient == null) throw IllegalArgumentException("Request without recipient is impossible.")

        // let you know
        request.recipient?.let {
            sendPacket(it.address, Packet.ofRefusingRequest(request))
        }
        
        // delete
        Realm.getDefaultInstance().executeTransaction {
            request.deleteFromRealm()
        }
    }
    override fun beRefusedConnectionRequest(packet: Packet) {
         Realm.getDefaultInstance().where(ConnectionRequest::class.java)
            .equalTo("connectionId", packet.connectionId)
            .equalTo("isInbound", true)
            .findFirst()
            ?.let { request -> request.deleteFromRealm() }
            ?: Timber.w("cannot find corresponding pending request for REFUSE_CONNECT.")
    }

    override fun sendUpdate(connectionId: Long) {
        val connection = getConnection(connectionId) ?: return
        val packet = Packet.ofSendingData(connection, locationRepo.getCurrentLocation() ?: return)

        sendPacket(connection.recipient?.address ?: return, packet)
    }
    override fun beSentUpdate(packet: Packet) {
        val realm = Realm.getDefaultInstance()

        realm.where(Connection::class.java)
            .equalTo("id", packet.connectionId)
            .findFirst()
            ?.let { connection ->
                realm.executeTransaction {
                    connection.lastUpdate = System.currentTimeMillis()
                    connection.latitude = packet.latitude
                    connection.longitude = packet.longitude
                }

                Timber.i("Received data updated.")

            } ?: Timber.w("cannot find corresponding connection for DATA.")
    }

    override fun requestUpdate(connectionId: Long) {
        val connection = getConnection(connectionId) ?: return
        val packet = Packet.ofRequestingUpdate(connection)

        sendPacket(connection.recipient?.address ?: return, packet)

        Timber.i("requested update")
    }
    override fun beRequestedUpdate(packet: Packet) {
         Realm.getDefaultInstance().where(Connection::class.java)
            .equalTo("id", packet.connectionId)
            .findFirst()
            ?.let { sendUpdate(it.id) }
            ?: Timber.w("cannot find corresponding connection for REQUEST_DATA.")
    }

    override fun requestDisconnect(connectionId: Long) {
        val realm = Realm.getDefaultInstance()

        val connection = getConnection(connectionId) ?: return
        val packet = Packet.ofRequestingDisconnect(connection)

        // stop sending
        unregisterTask(connection)

        // notify
        sendPacket(connection.recipient?.address ?: return, packet)

        // delete
        realm.executeTransaction {
            connection.deleteFromRealm()
        }

        realm.close()

        Timber.i("requested disconnect")
    }
    override fun beRequestedDisconnect(packet: Packet) {
        val connection = getConnection(packet.connectionId) ?: return

        sendPacket(connection.recipient?.address ?: return, packet)

         Realm.getDefaultInstance().executeTransaction {
            connection.deleteFromRealm()
        }
    }


    override fun sendPacket(address: String, packet: Packet) {
        try {
            val serialized = serializePacket(packet) ?: return

            SmsManager.getDefault().sendTextMessage(
                address,
                null,
                serialized,
                null,
                null
            )

            Timber.i("sent packet: \"$serialized\"")
        } catch (e: Exception) {
            Timber.w(e)
        }
    }

    override fun receivePacket(address: String, body: String) {
        try {
            val packet = parsePacket(body)?.apply {
                this.address = address
            } ?: return

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

                Packet.PacketType.DATA.number -> {
                    beSentUpdate(packet)
                }

                Packet.PacketType.REQUEST_DATA.number -> {
                    beRequestedUpdate(packet)
                }

                Packet.PacketType.REQUEST_DISCONNECT.number -> {
                    beRequestedDisconnect(packet)
                }
            }

            Timber.i("Successfully received packet.")

        } catch (e: Exception) {
            Timber.w(e)
        }
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
    override fun parsePacket(body: String): Packet? {
        /**
         * 예외처리
         */
        if (!isValidPacket(body)) {
            Timber.w("not a LocationSupport packet.")
            return null
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
            return null
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
            return null
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

                return null
            }

            json.addProperty(it.fieldName, value)
        }

        /**
         * Packet 객체 확보.
         */
        return try {
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
            return null
        }
    }

    /**
     * Serialize [Packet] to a plain string.
     * Two fixed field: type of packet and connection id.
     *
     * @see [parsePacket]
     * @see [Packet].
     */
    override fun serializePacket(packet: Packet): String? {
        val builder = StringBuilder().append(GEO_MMS_PREFIX)

        val type = Packet.PacketType.values().find { it.number == packet.type }

        if (type == null) {
            Timber.w("wrong packet type: ${packet.type}")
            return null
        }

        type.fields.forEach {
            val value = try {
                Reflection.readInstanceProperty<Any>(packet, it.fieldName).toString()
            }
            catch (e: Exception) {
                Timber.w("error occurred while accessing property.")
                return null
            }

            builder.append(value)

            if (it != type.fields.last()) {
                builder.append(FIELD_SPLITTER)
            }
        }

        return builder.toString()
    }

    override fun isValidPacket(body: String): Boolean {
        if (body.isBlank())                     return false   /* 비어있는 메시지 */
        if (!body.startsWith(GEO_MMS_PREFIX))   return false   /* 무관한 메시지 */

        return true
    }

    private fun findType(typeNum: Number): Packet.PacketType? {
        return Packet.PacketType.values().find { it.number == typeNum }
    }

    /**
     * Only once when app started.
     * Add scheduled tasks for active connections.
     */
    private fun restoreTasks() {
        scheduler.cancelAll()
        Realm.getDefaultInstance().executeTransaction {
            getConnections().forEach {
                if (it.isExpired()) {
                    it.deleteFromRealm()
                } else {
                    registerTask(it)
                }
            }

            // TODO find a way to remove outgoing requests.
            getOutgoingRequests().deleteAllFromRealm()
        }
    }

    /**
     * Register periodic update task of connection.
     * The connection id is good to use as a task id.
     *
     * TODO: UPDATE_INTERVAL hardcoded. fix it.
     *
     * @param connection must be a realm managed object.
     */
    private fun registerTask(connection: Connection) {
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
        Timber.i("Will be disconnected at ${DateTime(connection.due).toString()}")
    }

    /**
     * Unegister periodic update task of connection.
     *
     * @param connection must be a realm managed object.
     */
    private fun unregisterTask(connection: Connection) {
        scheduler.cancel(connection.id)

        Timber.i("Task unregistered: connection ${connection.id}.")
    }

    companion object {
        const val GEO_MMS_PREFIX = "[GEOMMS]"
        const val FIELD_SPLITTER = ":"

        const val UPDATE_INTERVAL = 1 * 60 * 1000L // 1 min
    }
}