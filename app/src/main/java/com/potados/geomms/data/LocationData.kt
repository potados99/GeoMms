package com.potados.geomms.data

import com.google.gson.annotations.SerializedName

/**
 * 위치정보 전달 단위
 */
data class LocationData(
    @SerializedName("latitude")     val latitude: Double,
    @SerializedName("longitude")    val longitude: Double,
    @SerializedName("date")         val createDate: Long
) {

    companion object {

    }
}