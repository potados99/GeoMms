package com.potados.geomms.data.entity

import com.potados.geomms.util.DateTime
import com.potados.geomms.util.Metric
import java.util.*

/**
 * LocationSupportManagerImpl 시스템의 연결 정보를 표현합니다.
 */
data class LocationSupportConnection(
    val person: LocationSupportPerson,
    val establishedTime: Long
) {

    /**
     * 연결 id
     */
    val connectionId: Int = generateConnectionId()

    /**
     * 마지막 송신 시간
     */
    var lastSentTime: DateTime? = null
    var lastReceivedTime: DateTime? = null

    /**
     * 마지막 송신 데이터
     */
    var lastSentPacket: LocationSupportPacket? = null
    var lastReceivedPacket: LocationSupportPacket? = null

    /**
     * 상대방 마지막 거리
     * 단위: m(미터)
     */
    var lastSeenDistance: Metric? = null


    init {

    }



    companion object {
        private fun generateConnectionId(): Int = 1

        fun a() {

        }
    }
}