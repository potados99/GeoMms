package com.potados.geomms.protocol

import com.potados.geomms.data.entity.LocationSupportConnection
import com.potados.geomms.data.entity.LocationSupportPerson

interface LocationSupportManager {

    /**
     * 새 연결을 생성합니다.
     */
    fun createNewConnection(person: LocationSupportPerson)

    /**
     * 연결 종료하기.
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
