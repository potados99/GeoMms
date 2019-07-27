package com.potados.geomms.feature.location

import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.os.Looper
import android.util.Log
import com.google.android.gms.maps.model.LatLng
import com.potados.geomms.core.extension.toLatLng
import com.potados.geomms.core.functional.Either
import com.potados.geomms.core.util.DateTime
import com.potados.geomms.core.util.Metric
import com.potados.geomms.feature.location.data.LocationRepository
import com.potados.geomms.feature.location.data.LocationSupportConnection
import com.potados.geomms.feature.location.data.LocationSupportPacket
import com.potados.geomms.feature.location.data.LocationSupportRequest
import com.potados.geomms.feature.message.data.MessageRepository
import com.potados.geomms.feature.message.data.SmsEntity
import org.koin.core.KoinComponent
import org.koin.core.inject

class LocationSupportServiceImpl(
    private val locationRepository: LocationRepository
) : LocationSupportService, KoinComponent {

    private val connections = mutableListOf<LocationSupportConnection>()

    private val pendingRequests = mutableListOf<LocationSupportRequest>()
    private val incomingRequests = mutableListOf<LocationSupportRequest>()

    private val messageRepository: MessageRepository by inject()

    init {
        restore()
    }

    override fun onPacketReceived(address: String, packet: LocationSupportPacket) {
        /**
         * 패킷 자체는 온전하다고 가정하고 처리해야 할 패킷만 처리합니다.
         */
        when (packet.type) {
            LocationSupportPacket.Companion.PacketType.REQUEST_CONNECT.number -> {
                val request = LocationSupportRequest.fromIncomingRequestPacket(address, packet)

                incomingRequests.add(request)
            }

            LocationSupportPacket.Companion.PacketType.ACCEPT_CONNECT.number -> {
                val request = pendingRequests.find { it.id == packet.connectionId } ?: return
                val connection = LocationSupportConnection.fromAcceptedRequest(request)

                pendingRequests.remove(request)
                connections.add(connection)
            }

            LocationSupportPacket.Companion.PacketType.DATA.number -> {
                val connection = connections.find { it.id == packet.connectionId } ?: return

                updateConnectionWithInboundPacket(connection, packet)
            }

            LocationSupportPacket.Companion.PacketType.REQUEST_DATA.number -> {
                val connection = connections.find { it.id == packet.connectionId } ?: return

                sendUpdate(connection)
            }

            LocationSupportPacket.Companion.PacketType.REQUEST_DISCONNECT.number -> {
                // ...
            }
        }
    }

    override fun requestNewConnection(request: LocationSupportRequest) {
        val address = request.person.address
        val packetToSend = LocationSupportPacket.ofCreatingNewRequest(request)

        sendPacket(address, packetToSend)

        pendingRequests.add(request)
    }

    override fun acceptNewConnection(request: LocationSupportRequest) {
        if (incomingRequests.indexOf(request) == -1) {
            /**
             * 연결을 수락하는 행위는 그 이전에 해당 요청이 존재해야 함을 전제로 합니다.
             * 따라서 이에 해당하지 않는 경우에는 예외를 던집니다.
             */
            throw IllegalArgumentException()
        }

        val address = request.person.address
        val packetToSend = LocationSupportPacket.ofAcceptingRequest(request)

        sendPacket(address, packetToSend)

        /**
         * 요청을 수락했으니 해당 요청은 대기 목록에서 제거하고 새로운 연결을 추가합니다.
         */
        incomingRequests.remove(request)
        connections.add(LocationSupportConnection.fromAcceptedRequest(request))
    }

    override fun requestUpdate(connection: LocationSupportConnection) {
        val address = connection.person.address
        val packetToSend = LocationSupportPacket.ofRequestingUpdate(connection)

        sendPacket(address, packetToSend)
    }

    override fun sendUpdate(connection: LocationSupportConnection) {
        val address = connection.person.address
        val packetToSend = LocationSupportPacket.ofSendingData(
            connection,
            locationRepository.getCurrentLocation() ?: throw IllegalStateException()
        )

        sendPacket(address, packetToSend)

        /**
         * 패킷을 보냈으니 connection을 업데이트해줍니다.
         */
        updateConnectionWithOutboundPacket(connection, packetToSend)
    }

    override fun deleteConnection(connection: LocationSupportConnection) {

    }

    override fun getInboundRequests(): List<LocationSupportRequest> {
        return incomingRequests
    }

    override fun getOutboundRequests(): List<LocationSupportRequest> {
        return pendingRequests
    }

    override fun getConnections(): List<LocationSupportConnection> {
        return connections
    }

    private fun sendPacket(address: String, packet: LocationSupportPacket) {
        val serialized = LocationSupportProtocol.serialize(packet) ?:
        throw IllegalArgumentException()

        messageRepository.sendSms(
            SmsEntity()
                .address(address)
                .body(serialized)
            , false /* 저장 안함. */
        ).either({ throw IllegalArgumentException() }, {})
    }

    private fun restore() {
        /**
         * 내장 DB로부터 기존의 연결과 요청들을 불러와 복구합니다.
         */
    }

    private fun updateConnectionWithInboundPacket(connection: LocationSupportConnection, packet: LocationSupportPacket) {
        with(connection) {
            lastReceivedTime = DateTime.now()
            lastReceivedPacket = packet

            val personLocation = Location("").apply {
                latitude = packet.latitude
                longitude = packet.longitude
            }

            /*
            currentDistance = Metric.fromDistanceBetween(
                locationRepository.getCurrentLocation() ?: throw IllegalStateException()
                , personLocation
            )
            */
        }
    }

    private fun updateConnectionWithOutboundPacket(connection: LocationSupportConnection, packet: LocationSupportPacket) {
        with(connection) {
            lastSentTime = DateTime.now()
            lastSentPacket = packet
        }
    }
}