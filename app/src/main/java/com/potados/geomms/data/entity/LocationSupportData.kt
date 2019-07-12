package com.potados.geomms.data.entity

import com.google.gson.annotations.SerializedName

/**
 * LocationSupport 시스템의 정보 전달 단위입니다.
 *
 * SerializedName은 안씁니다. 안써도 잘 작동해요.
 */
data class LocationSupportData(
    val latitude: Double,
    val longitude: Double,
    val date: Long
) {

}