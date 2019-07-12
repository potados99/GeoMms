package com.potados.geomms.protocol

import com.potados.geomms.data.entity.LocationSupportConnection
import com.potados.geomms.data.entity.LocationSupportPacket
import com.potados.geomms.data.entity.LocationSupportPerson

interface LocationSupportManager {

    /**
     * LocationSupport 패킷이 도착했을 때의 동작을 지정합니다.
     */
    fun onPacketReceived(packet: LocationSupportPacket)

    /**
     * 새 연결을 생성하는 요청을 날립니다.
     */
    fun requestNewConnection(person: LocationSupportPerson)

    /**
     * 새 연결 요청을 수락합니다.
     */
    fun acceptNewConnection(person: LocationSupportPerson, reqPacket: LocationSupportPacket)

    /**
     * 연결을 종료합니다.
     */
    fun deleteConnection(connection: LocationSupportConnection)

    /**
     * 현재 연결의 목록을 가져옵니다.
     */
    fun getConnections(): List<LocationSupportConnection>

    /**
     * 상대방에게 현재 위치를 알려줄 것을 요청합니다.
     */
    fun requestUpdate(connection: LocationSupportConnection)

    /**
     * 상대방에게 내 현재 위치를 보냅니다.
     */
    fun sendUpdate(connection: LocationSupportConnection)

}
