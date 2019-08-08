package com.potados.geomms.feature.location.data

import com.potados.geomms.util.DateTime
import com.potados.geomms.util.Metric
import com.potados.geomms.feature.common.Person

/**
 * LSServiceImpl 시스템의 연결 정보를 표현합니다.
 */
data class LSConnection(
    val id: Int,
    val person: Person,
    val lifeSpan: Long,
    val establishedTime: Long
) {

    var lastSentPacket: LSPacket? = null
    var lastReceivedPacket: LSPacket? = null

    var lastSentTime: DateTime? = null
    var lastReceivedTime: DateTime? = null

    /**
     * 상대방 마지막 거리.
     * 단위: m(미터)
     */
    var currentDistance: Metric? = null

    companion object {
        fun fromAcceptedRequest(request: LSRequest, time: Long? = null) =
            LSConnection(
                request.id,
                request.person,
                request.lifeSpan,
                time ?: DateTime.getCurrentTimeStamp()
            )
    }
}