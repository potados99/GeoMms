package com.potados.geomms.data.entity

import com.google.gson.annotations.SerializedName

/**
 * LocationSupportManagerImpl 시스템의 정보 전달 단위입니다.
 *
 * SerializedName은 안씁니다. 안써도 잘 작동해요.
 */
data class LocationSupportPacket(
    val type: Int,
    val latitude: Double,
    val longitude: Double,
    val date: Long
) {
}