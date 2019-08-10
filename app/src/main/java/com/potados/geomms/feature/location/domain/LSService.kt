package com.potados.geomms.feature.location.domain

import com.potados.geomms.feature.location.data.LSConnection
import com.potados.geomms.feature.location.data.LSPacket
import com.potados.geomms.feature.location.data.LSRequest

/**
 * SMS 통신을 하며 위치정보를 주고받습니다.
 *
 * 이 자체로는 SMS를 수신하지 못하기 때문에 최상단(Fragment)에서
 * Use Case 호출을 통해 onPacketReceived를 실행합니다.
 * 그 결과는 다시 getConnections 등을 통해 최상단까지 전파됩니다.
 */
interface LSService {

    /**
     * 스트링이 적절한 LSPacket인지 검사합니다.
     */
    fun isLocationSupportMessage(body: String): Boolean

    /**
     * 직렬화된 패킷의 parse를 시도합니다.
     */
    fun parse(body: String): LSPacket?

    /**
     * 패킷의 직렬화를 시도합니다.
     */
    fun serialize(locationPacket: LSPacket): String?

    /**
     * LocationSupport 패킷이 도착했을 때의 동작을 지정합니다.
     * 패킷이 도착했을 때에 이 메소드가 호출되어야 그에 맞는 행동이 수행됩니다.
     */
    fun onPacketReceived(address: String, body: String)

    /**
     * 새 연결을 생성하는 요청을 날립니다.
     */
    fun requestNewConnection(request: LSRequest)

    /**
     * 새 연결 요청을 수락합니다.
     */
    fun acceptNewConnection(request: LSRequest)

    /**
     * 상대방에게 현재 위치를 알려줄 것을 요청합니다.
     */
    fun requestUpdate(connection: LSConnection)

    /**
     * 상대방에게 내 현재 위치를 보냅니다.
     */
    fun sendUpdate(connection: LSConnection)

    /**
     * 연결을 종료합니다.
     */
    fun deleteConnection(connection: LSConnection)


    /**
     * 수락 대기중인 들어오는 요청을 가져옵니다.
     */
    fun getInboundRequests(): List<LSRequest>

    /**
     * 상대방의 수락을 대기중인 나가는 요청을 가져옵니다.
     */
    fun getOutboundRequests(): List<LSRequest>

    /**
     * 현재 연결의 목록을 가져옵니다.
     */
    fun getConnections(): List<LSConnection>

}
