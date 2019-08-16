package com.potados.geomms.service

import android.location.Location
import android.telephony.PhoneNumberUtils
import android.telephony.SmsManager
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.google.gson.JsonSyntaxException
import com.potados.geomms.manager.KeyManager
import com.potados.geomms.model.Connection
import com.potados.geomms.model.ConnectionRequest
import com.potados.geomms.model.Packet
import com.potados.geomms.model.Recipient
import com.potados.geomms.repository.ConversationRepository
import com.potados.geomms.repository.LocationRepository
import com.potados.geomms.util.Reflection
import com.potados.geomms.util.Types
import io.realm.Realm
import io.realm.RealmResults
import io.realm.Sort
import timber.log.Timber

class LocationSupportServiceImpl(
    private val conversationRepo: ConversationRepository,
    private val locationRepo: LocationRepository,
    private val keyManager: KeyManager
) : LocationSupportService {

    private val realm = Realm.getDefaultInstance()

    override fun getConnections(): RealmResults<Connection> {
        return realm.where(Connection::class.java)
            .sort("date", Sort.ASCENDING)
            .findAll()
    }

    override fun getConnection(id: Long): Connection {
        return realm.where(Connection::class.java)
            .equalTo("id", id)
            .findFirst() ?: throw IllegalArgumentException("connection of id $id does not exist.")
    }

    private fun getRecipient(address: String): Recipient {
        val found = realm.where(Recipient::class.java)
            .equalTo("address", PhoneNumberUtils.stripSeparators(address))
            .findFirst()

        return if (found != null) found
        else {
            conversationRepo.getOrCreateConversation(address)
            /**
             * 넘겨받은 주소에 해당하는 새로운 conversation이 만들어졌으니
             * 이에 해당하는 recipient가 realm에 저장되었을 것이 보장됩니다.
             */
            realm.where(Recipient::class.java)
                .equalTo("address", PhoneNumberUtils.stripSeparators(address))
                .findFirst() ?: throw RuntimeException("recipient of address $address must exist.")
        }
    }

    override fun removeConnection(id: Long) {
        val connection = realm.where(Connection::class.java)
            .equalTo("id", id)
            .findAll()

        realm.executeTransaction {
            connection.deleteAllFromRealm()
        }
    }

    override fun getIncomingRequests(): RealmResults<ConnectionRequest> {
        return realm.where(ConnectionRequest::class.java)
            .sort("date", Sort.ASCENDING)
            .equalTo("isInbound", true)
            .findAll()
    }

    override fun getOutgoingRequests(): RealmResults<ConnectionRequest> {
        return realm.where(ConnectionRequest::class.java)
            .sort("date", Sort.ASCENDING)
            .equalTo("isInbound", false)
            .findAll()
    }

    /**
     * Create a new connection request and send it to recipient.
     *
     * @return The outbound request. Managed realm object.
     */
    override fun requestNewConnection(address: String, duration: Long): ConnectionRequest {

        val recipient = getRecipient(address)

        var request = ConnectionRequest(
            connectionId = keyManager.randomId(99999),
            recipient = recipient,
            isInbound = false,
            date = System.currentTimeMillis(),
            duration = duration
        )

        sendPacket(address, Packet.ofRequestingNewConnection(request))

        realm.executeTransaction { request = it.copyToRealm(request) }

        return request
    }

    /**
     * Accept inbound connection request and create a connection upon it.
     * If there exists any connection where its ID equals the request ID,
     * do nothing and return it.
     *
     * @return Managed Connection object
     */
    override fun acceptConnectionRequest(request: ConnectionRequest): Connection {
        if (!request.isInbound) throw IllegalAccessError("Cannot accept request not heading to isInbound.")

        val found = realm
            .where(Connection::class.java)
            .equalTo("id", request.connectionId)
            .findFirst()

        if (found != null) return found

        var connection = Connection.fromAcceptedRequest(request)

        realm.executeTransaction { realm -> connection = realm.copyToRealm(connection) }

        return connection
    }


    override fun sendUpdate(connectionId: Long) {
        val connection = getConnection(connectionId)
        val packet = Packet.ofSendingData(connection, locationRepo.getCurrentLocation() ?: return)

        sendPacket(connection.recipient?.address ?: return, packet)
    }

    override fun requestUpdate(connectionId: Long) {
        val connection = getConnection(connectionId)
        val packet = Packet.ofRequestingUpdate(connection)

        sendPacket(connection.recipient?.address ?: return, packet)
    }

    override fun requestDisconnect(connectionId: Long) {
        val connection = getConnection(connectionId)
        val packet = Packet.ofRequestingDisconnect(connection)

        sendPacket(connection.recipient?.address ?: return, packet)
    }


    override fun sendPacket(address: String, packet: Packet) {
        val serialized = serializePacket(packet) ?: return

        SmsManager.getDefault().sendTextMessage(
            address,
            null,
            serialized,
            null,
            null
        )
    }

    override fun receivePacket(address: String, body: String) {
        try {
            val packet = parsePacket(body) ?: return
            packet.address = address

            Timber.v("Packet parse success.")

            realm.beginTransaction()

            when (packet.type) {

                /**
                 * 연결 요청에 대한 패킷일 때.
                 */
                Packet.PacketType.REQUEST_CONNECT.number -> {
                    val request = ConnectionRequest(
                        connectionId = packet.connectionId,
                        recipient = getRecipient(packet.address),
                        isInbound = true,
                        date = packet.date,
                        duration = packet.duration
                    )
                    realm.insertOrUpdate(request)
                }

                /**
                 * 연결 요청을 수락하는 패킷일 때.
                 */
                Packet.PacketType.ACCEPT_CONNECT.number -> {
                    realm.where(ConnectionRequest::class.java)
                        .equalTo("connectionId", packet.connectionId)
                        .equalTo("isInbound", true)
                        .findFirst()
                        ?.let { realm.insertOrUpdate(Connection.fromAcceptedRequest(it)) }
                        ?: Timber.w("cannot find corresponding pending request for ACCEPT_CONNECT.")
                }

                /**
                 * 연결 요청을 거절하는 패킷일 때.
                 */
                Packet.PacketType.REFUSE_CONNECT.number -> {
                    realm.where(ConnectionRequest::class.java)
                        .equalTo("connectionId", packet.connectionId)
                        .equalTo("isInbound", true)
                        .findFirst()
                        ?.let { request -> request.deleteFromRealm() }
                        ?: Timber.w("cannot find corresponding pending request for REFUSE_CONNECT.")
                }

                /**
                 * 위치정보에 대한 패킷일 때.
                 */
                Packet.PacketType.DATA.number -> {
                    realm.where(Connection::class.java)
                        .equalTo("id", packet.connectionId)
                        .findFirst()
                        ?.let { connection ->
                            connection.lastUpdate = System.currentTimeMillis()
                            connection.latitude = packet.latitude
                            connection.longitude = packet.longitude
                        } ?: Timber.w("cannot find corresponding connection for DATA.")
                }

                /**
                 * 위치정보 요청에 대한 패킷일 때.
                 */
                Packet.PacketType.REQUEST_DATA.number -> {
                    realm.where(Connection::class.java)
                        .equalTo("id", packet.connectionId)
                        .findFirst()
                        ?.let { sendUpdate(it.id) }
                        ?: Timber.w("cannot find corresponding connection for REQUEST_DATA.")
                }

                /**
                 * 연결 종료 요청에 대한 패킷일 때.
                 */
                Packet.PacketType.REQUEST_DISCONNECT.number -> {
                    // TODO
                }
            }

            realm.commitTransaction()
            realm.close()

        } catch (e: Exception) {
            Timber.w(e)
        }
    }

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
         * LSPacket 객체 확보.
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

    companion object {
        const val GEO_MMS_PREFIX = "[GEOMMS]"
        const val FIELD_SPLITTER = ":"
    }
}