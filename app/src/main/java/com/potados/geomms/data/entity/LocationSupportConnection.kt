package com.potados.geomms.data.entity

/**
 * LocationSupportManagerImpl 시스템의 연결 정보를 표현합니다.
 */
data class LocationSupportConnection(
    val buddy: LocationSupportPerson,
    val establishedTime: Long
) {

    /**
     * 마지막 송신 시간
     */
    var lastSentTime: Long = 0L
    var lastReceivedTime: Long = 0L

    /**
     * 마지막 송신 데이터
     */
    var lastSentPacket: LocationSupportPacket? = null
    var lastReceivedPacket: LocationSupportPacket? = null

    companion object {

    }
}