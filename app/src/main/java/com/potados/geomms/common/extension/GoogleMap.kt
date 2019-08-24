package com.potados.geomms.common.extension

import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.LatLng

fun GoogleMap.moveTo(lat: Double, lng: Double, zoom: Float = 10f) {
    moveCamera(CameraUpdateFactory.newLatLng(LatLng(lat, lng)))
    animateCamera(CameraUpdateFactory.zoomTo(zoom))

}