package com.potados.geomms.data.entity

import com.google.gson.annotations.SerializedName

/**
 * LocationSupportManagerImpl 시스템의 정보 전달 단위입니다.
 * 아래 속성들의 상세 용도는 com.potados.geomms.protocol.LocationSupportPacket을 참고하세요.
 */
data class LocationSupportPacket(
    val type: Int,
    val id: Int,
    val span: Long,
    val latitude: Double,
    val longitude: Double
) {
}