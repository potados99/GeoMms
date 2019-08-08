package com.potados.geomms.feature.location.domain

import android.util.Log
import com.potados.geomms.core.functional.Result
import com.potados.geomms.core.interactor.UseCase.None
import com.potados.geomms.util.DateTime
import com.potados.geomms.feature.common.MessageService
import com.potados.geomms.feature.location.data.LocationRepository
import com.potados.geomms.feature.location.data.LSConnection
import com.potados.geomms.feature.location.data.LSPacket
import com.potados.geomms.feature.location.data.LSRequest
import com.potados.geomms.feature.message.domain.SmsComposed
import org.koin.core.KoinComponent

class LSServiceImpl(
    private val locationRepository: LocationRepository,
    private val messageService: MessageService
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

    override fun onPacketReceived(address: String, packet: LSPacket): Result<LSPacket> {
        try {
            /**
             * 패킷 자체는 온전하다고 가정하고 처리해야 할 패킷만 처리합니다.
             */
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
                    connections.find { it.id == packet.connectionId }?.let(::sendUpdate)
                        ?: Log.w("LSServiceImpl:onPacketReceived", "cannot find corresponding connection for REQUEST_DATA.")
                }

                /**
                 * 연결 종료 요청에 대한 패킷일 때.
                 */
                LSPacket.Companion.PacketType.REQUEST_DISCONNECT.number -> {
                    // ...
                }
            }
        } catch (e: Exception) {
            return Result.Error(e)
        }

        return Result.Success(packet)
    }

    override fun onPacketReceived(address: String, packet: String): Result<LSPacket> {
        LSProtocol.parse(packet)?.let {
            return onPacketReceived(address, it)
        } ?: return Result.Error(IllegalArgumentException("Not a Location Support packet."))
    }

    override fun requestNewConnection(request: LSRequest): Result<None> {
        try {
            val address = request.person.phoneNumber
            val packetToSend = LSPacket.ofCreatingNewRequest(request)

            sendPacket(address, packetToSend)

            pendingRequests.add(request)
        } catch (e: Exception) {
            return Result.Error(e)
        }

        return Result.Success(None())
    }

    override fun acceptNewConnection(request: LSRequest): Result<None> {
        try {
            if (incomingRequests.indexOf(request) == -1) {
                /**
                 * 연결을 수락하는 행위는 그 이전에 해당 요청이 존재해야 함을 전제로 합니다.
                 * 따라서 이에 해당하지 않는 경우에는 예외를 던집니다.
                 */
                throw IllegalArgumentException()
            }

            val address = request.person.phoneNumber
            val packetToSend = LSPacket.ofAcceptingRequest(request)

            sendPacket(address, packetToSend)

            /**
             * 요청을 수락했으니 해당 요청은 대기 목록에서 제거하고 새로운 연결을 추가합니다.
             */
            incomingRequests.remove(request)
            connections.add(LSConnection.fromAcceptedRequest(request))
        } catch (e: Exception) {
            return Result.Error(e)
        }

        return Result.Success(None())
    }

    override fun requestUpdate(connection: LSConnection): Result<None> {
        try {
            val address = connection.person.phoneNumber
            val packetToSend = LSPacket.ofRequestingUpdate(connection)

            sendPacket(address, packetToSend)
        } catch (e: Exception) {
            return Result.Error(e)
        }

        return Result.Success(None())
    }

    override fun sendUpdate(connection: LSConnection): Result<None> {
        try {
            val address = connection.person.phoneNumber
            val packetToSend = LSPacket.ofSendingData(
                connection,
                locationRepository.getCurrentLocation() ?: throw IllegalStateException()
            )

            sendPacket(address, packetToSend)

            /**
             * 패킷을 보냈으니 connection을 업데이트해줍니다.
             */
            updateConnectionWithOutboundPacket(connection, packetToSend)
        } catch (e: Exception) {
            return Result.Error(e)
        }

        return Result.Success(None())
    }

    override fun deleteConnection(connection: LSConnection): Result<None> {
        try {
            // TODO

        } catch (e: Exception) {
            return Result.Error(e)
        }

        return Result.Success(None())
    }

    override fun getInboundRequests(): Result<List<LSRequest>> {
        return Result.Success(incomingRequests)
    }

    override fun getOutboundRequests(): Result<List<LSRequest>> {
        return Result.Success(pendingRequests)
    }

    override fun getConnections(): Result<List<LSConnection>> {
        return Result.Success(connections)
    }


    private fun sendPacket(address: String, packet: LSPacket) {
        val serialized = LSProtocol.serialize(packet)
            ?: throw IllegalArgumentException()

        messageService.sendSms(SmsComposed(address, serialized), false)
            .onError { throw IllegalArgumentException() }
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
                locationRepository.getCurrentLocation() ?: throw IllegalStateException()
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
}