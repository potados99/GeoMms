package com.potados.geomms.feature.location.data

/**
 * LocationSupportServiceImpl 시스템의 정보 전달 단위입니다.
 * 아래 속성들의 상세 용도는 com.potados.geomms.feature.protocol.LocationSupportPacket을 참고하세요.
 */
data class LocationSupportPacket(
    val type: Int,
    val id: Int,
    val span: Long,
    val latitude: Double,
    val longitude: Double
)