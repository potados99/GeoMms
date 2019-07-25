package com.potados.geomms.feature.location.data

import com.potados.geomms.core.util.DateTime
import com.potados.geomms.core.util.Metric

/**
 * LocationSupportServiceImpl 시스템의 연결 정보를 표현합니다.
 */
data class LocationSupportConnection(
    val id: Long,
    val person: LocationSupportPerson,
    val lifeSpan: Long,
    val establishedTime: Long
) {

    var lastSentPacket: LocationSupportPacket? = null
    var lastReceivedPacket: LocationSupportPacket? = null

    var lastSentTime: DateTime? = null
    var lastReceivedTime: DateTime? = null

    /**
     * 상대방 마지막 거리.
     * 단위: m(미터)
     */
    var lastSeenDistance: Metric? = null

    companion object {
        fun fromAcceptedRequest(request: LocationSupportRequest, time: Long? = null) =
            LocationSupportConnection(
                request.id,
                request.person,
                request.lifeSpan,
                time ?: DateTime.getCurrentTimeStamp()
            )
    }
}