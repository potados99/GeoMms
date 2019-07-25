package com.potados.geomms.feature.location

import androidx.lifecycle.LiveData
import com.potados.geomms.feature.location.data.LocationSupportConnection
import com.potados.geomms.feature.location.data.LocationSupportPacket
import com.potados.geomms.feature.location.data.LocationSupportPerson
import com.potados.geomms.feature.location.data.LocationSupportRequest

interface LocationSupportService {

    /**
     * LocationSupport 패킷이 도착했을 때의 동작을 지정합니다.
     */
    fun onPacketReceived(packet: LocationSupportPacket, address: String)

    /**
     * 새 연결을 생성하는 요청을 날립니다.
     */
    fun requestNewConnection(request: LocationSupportRequest)

    /**
     * 새 연결 요청을 수락합니다.
     */
    fun acceptNewConnection(request: LocationSupportRequest)

    /**
     * 상대방에게 현재 위치를 알려줄 것을 요청합니다.
     */
    fun requestUpdate(connection: LocationSupportConnection)

    /**
     * 상대방에게 내 현재 위치를 보냅니다.
     */
    fun sendUpdate(connection: LocationSupportConnection)

    /**
     * 연결을 종료합니다.
     */
    fun deleteConnection(connection: LocationSupportConnection)

    /**
     * 수락 대기중인 들어오는 요청을 가져옵니다.
     */
    fun getInboundRequests(): List<LocationSupportRequest>

    /**
     * 상대방의 수락을 대기중인 나가는 요청을 가져옵니다.
     */
    fun getOutboundRequests(): List<LocationSupportRequest>

    /**
     * 현재 연결의 목록을 가져옵니다.
     */
    fun getConnections(): List<LocationSupportConnection>

}
