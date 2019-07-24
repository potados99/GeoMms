package com.potados.geomms.feature.location

import androidx.lifecycle.LiveData

interface LocationSupportService {

    /**
     * LocationSupport 패킷이 도착했을 때의 동작을 지정합니다.
     */
    fun onPacketReceived(packet: LocationSupportPacket, address: String)

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
    fun getConnections(): LiveData<List<LocationSupportConnection>>

    /**
     * 상대방에게 현재 위치를 알려줄 것을 요청합니다.
     */
    fun requestUpdate(connection: LocationSupportConnection)

    /**
     * 상대방에게 내 현재 위치를 보냅니다.
     */
    fun sendUpdate(connection: LocationSupportConnection)

    /**
     * Connection들을 매초마다 업데이트합니다.
     */
    fun onEverySecondUpdate()

}
