package com.potados.geomms.data.entity

import com.potados.geomms.core.util.DateTime
import com.potados.geomms.core.util.Metric

/**
 * LocationSupportManagerImpl 시스템의 연결 정보를 표현합니다.
 */
data class LocationSupportConnection(
    val person: LocationSupportPerson,
    var establishedTime: Long
) {

    /**
     * 연결 id.
     */
    var connectionId: Int = generateConnectionId()

    /**
     * 마지막 발신 패킷.
     */
    var lastSentPacket: LocationSupportPacket? = null

    /**
     * 마지막 수신 패킷.
     */
    var lastReceivedPacket: LocationSupportPacket? = null

    /**
     * 마지막 발신 시간.
     * lastSentPacket에 의존적임.
     */
    var lastSentTime: DateTime? = null

    /**
     * 마지막 수신 시간.
     */
    var lastReceivedTime: DateTime? = null

    /**
     * 상대방 마지막 거리.
     * 단위: m(미터)
     */
    var lastSeenDistance: Metric? = null

    init {

    }

    companion object {
        private fun generateConnectionId(): Int = 1

    }
}