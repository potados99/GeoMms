/*
 * Copyright (C) 2019 Song Byeong Jun <potados99@gmail.com>
 *
 * This file is part of GeoMms.
 *
 * GeoMms is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * GeoMms is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with GeoMms.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.potados.geomms.common.extension

import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.LatLng
import android.R.attr.y
import android.R.attr.x

fun GoogleMap.moveTo(lat: Double, lng: Double, zoom: Float = 10f, bias: Boolean = true) {
    val point = projection.toScreenLocation(LatLng(lat, lng))

    point.set(point.x, point.y + if (bias) 200 else 0)

    animateCamera(CameraUpdateFactory.newLatLngZoom(projection.fromScreenLocation(point), zoom))
}