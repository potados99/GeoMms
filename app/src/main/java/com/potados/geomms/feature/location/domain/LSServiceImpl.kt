package com.potados.geomms.feature.location.domain

import android.util.Log
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.google.gson.JsonSyntaxException
import com.potados.geomms.util.DateTime
import com.potados.geomms.feature.location.data.LSConnection
import com.potados.geomms.feature.location.data.LSPacket
import com.potados.geomms.feature.location.data.LSRequest
import com.potados.geomms.repository.LocationRepository
import com.potados.geomms.repository.MessageRepository
import com.potados.geomms.util.Reflection
import com.potados.geomms.util.Types
import org.koin.core.KoinComponent
import timber.log.Timber

class LSServiceImpl(
    private val locationRepo: LocationRepository,
    private val messageRepo: MessageRepository
) : LSService, KoinComponent {

    /**
     * Repository와 Service를 분리하지 않았습니다.
     */
    private val connections = mutableListOf<LSConnection>()

    private val pendingRequests = mutableListOf<LSRequest>()
    private val incomingRequests = mutableListOf<LSRequest>()


    init {
        restore()
    }


    override fun parse(body: String): LSPacket? {
        /**
         * 예외처리
         */
        if (!isLocationSupportMessage(body)) {
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
        val typeNumber = with(LSPacket.Companion.Field.TYPE) {
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
            Gson().fromJson(json, Types.typeOf<LSPacket>())
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

    override fun isLocationSupportMessage(body: String): Boolean {
        if (body.isBlank())                     return false   /* 비어있는 메시지 */
        if (!body.startsWith(GEO_MMS_PREFIX))   return false   /* 무관한 메시지 */

        return true
    }

    override fun serialize(locationPacket: LSPacket): String? {

        val builder = StringBuilder().append(GEO_MMS_PREFIX)

        val type = LSPacket.Companion.PacketType.values().find { it.number == locationPacket.type }

        if (type == null) {
            Timber.w("wrong packet type: ${locationPacket.type}")
            return null
        }

        type.fields.forEach {
            val value = try {
                Reflection.readInstanceProperty<Any>(locationPacket, it.fieldName).toString()
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


    override fun onPacketReceived(address: String, body: String) {
        try {
            val packet = parse(body) ?: return

            Timber.v("Packet parese success.")

            when (packet.type) {

                /**
                 * 연결 요청에 대한 패킷일 때.
                 */
                LSPacket.Companion.PacketType.REQUEST_CONNECT.number -> {
                    val request = LSRequest.fromIncomingRequestPacket(address, packet)

                    incomingRequests.add(request)
                }

                /**
                 * 연결 요청을 수락하는 패킷일 때.
                 */
                LSPacket.Companion.PacketType.ACCEPT_CONNECT.number -> {
                    pendingRequests.find { it.id == packet.connectionId }?.let {
                        val connection = LSConnection.fromAcceptedRequest(it)

                        pendingRequests.remove(it)
                        connections.add(connection)
                    } ?: Log.w("LSServiceImpl:onPacketReceived", "cannot find corresponding pending request for ACCEPT_CONNECT.")
                }

                /**
                 * 연결 요청을 거절하는 패킷일 때.
                 */
                LSPacket.Companion.PacketType.REFUSE_CONNECT.number -> {
                    pendingRequests.find { it.id == packet.connectionId }?.let {
                        pendingRequests.remove(it)
                    } ?: Log.w("LSServiceImpl:onPacketReceived", "cannot find corresponding pending request for REFUSE_CONNECT.")
                }

                /**
                 * 위치정보에 대한 패킷일 때.
                 */
                LSPacket.Companion.PacketType.DATA.number -> {
                    connections.find { it.id == packet.connectionId }?.let {
                        updateConnectionWithInboundPacket(it, packet)
                    } ?: Log.w("LSServiceImpl:onPacketReceived", "cannot find corresponding connection for DATA.")
                }

                /**
                 * 위치정보 요청에 대한 패킷일 때.
                 */
                LSPacket.Companion.PacketType.REQUEST_DATA.number -> {
                        connections
                            .find { it.id == packet.connectionId }
                            ?.let(::sendUpdate)
                            ?: Timber.w("cannot find corresponding connection for REQUEST_DATA.")
                }

                /**
                 * 연결 종료 요청에 대한 패킷일 때.
                 */
                LSPacket.Companion.PacketType.REQUEST_DISCONNECT.number -> {
                    // ...
                }
            }
        } catch (e: Exception) {
            Timber.w(e)
        }
    }

    override fun requestNewConnection(request: LSRequest) {
        try {
            val address = request.person.address
            val packetToSend = LSPacket.ofCreatingNewRequest(request)

            sendPacket(address, packetToSend)

            pendingRequests.add(request)
        } catch (e: Exception) {
            Timber.w(e)
        }
    }

    override fun acceptNewConnection(request: LSRequest) {
        try {
            if (incomingRequests.indexOf(request) == -1) {
                /**
                 * 연결을 수락하는 행위는 그 이전에 해당 요청이 존재해야 함을 전제로 합니다.
                 * 따라서 이에 해당하지 않는 경우에는 예외를 던집니다.
                 */
                throw IllegalArgumentException()
            }

            val address = request.person.address
            val packetToSend = LSPacket.ofAcceptingRequest(request)

            sendPacket(address, packetToSend)

            /**
             * 요청을 수락했으니 해당 요청은 대기 목록에서 제거하고 새로운 연결을 추가합니다.
             */
            incomingRequests.remove(request)
            connections.add(LSConnection.fromAcceptedRequest(request))
        } catch (e: Exception) {
              Timber.w(e)
        }
    }

    override fun requestUpdate(connection: LSConnection) {
        try {
            val address = connection.person.address
            val packetToSend = LSPacket.ofRequestingUpdate(connection)

            sendPacket(address, packetToSend)
        } catch (e: Exception) {
              Timber.w(e)
        }
    }

    override fun sendUpdate(connection: LSConnection) {
        try {
            val address = connection.person.address
            val packetToSend = LSPacket.ofSendingData(
                connection,
                locationRepo.getCurrentLocation() ?: throw IllegalStateException()
            )

            sendPacket(address, packetToSend)

            /**
             * 패킷을 보냈으니 connection을 업데이트해줍니다.
             */
            updateConnectionWithOutboundPacket(connection, packetToSend)
        } catch (e: Exception) {
              Timber.w(e)
        }

          
    }

    override fun deleteConnection(connection: LSConnection) {
        try {
            // TODO

        } catch (e: Exception) {
              Timber.w(e)
        }

          
    }

    override fun getInboundRequests(): List<LSRequest> {
        return incomingRequests
    }

    override fun getOutboundRequests(): List<LSRequest> {
        return pendingRequests
    }

    override fun getConnections(): List<LSConnection> {
        return connections
    }


    private fun sendPacket(address: String, packet: LSPacket) {
        val serialized = LSProtocol.serialize(packet)
            ?: throw IllegalArgumentException()

        // messageRepo.sendMessage()
    }

    private fun restore() {
        /**
         * 내장 DB로부터 기존의 연결과 요청들을 불러와 복구합니다.
         */
    }

    private fun updateConnectionWithInboundPacket(connection: LSConnection, packet: LSPacket) {
        with(connection) {
            lastReceivedTime = DateTime.now()
            lastReceivedPacket = packet

            /*
            val personLocation = Location("").apply {
                latitude = packet.latitude
                longitude = packet.longitude
            }

            currentDistance = Metric.fromDistanceBetween(
                locationRepo.getCurrentLocation() ?: throw IllegalStateException()
                , personLocation
            )
            */
        }
    }

    private fun updateConnectionWithOutboundPacket(connection: LSConnection, packet: LSPacket) {
        with(connection) {
            lastSentTime = DateTime.now()
            lastSentPacket = packet
        }
    }




    private fun findType(typeNum: Number): LSPacket.Companion.PacketType? {
        return LSPacket.Companion.PacketType.values().find { it.number == typeNum }
    }

    companion object {
        private const val GEO_MMS_PREFIX = "[GEOMMS]"
        private const val FIELD_SPLITTER = ':'
    }
}