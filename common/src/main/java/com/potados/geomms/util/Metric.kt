/*
 * Copyright (C) 2019-2020 Song Byeong Jun <potados99@gmail.com>
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

/**
 * Copyright (C) 2019 Song Byeong Jun and original authors
 *
 * This file is part of GeoMms.
 *
 * This software makes use of third-party patent which belongs to 
 * KANG MOON KYOU and LEE GWI BONG:
 * System and Method for sharing service of location information
 * 10-1235884-0000 (2013.02.15)
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

package com.potados.geomms.util

import android.location.Location

class Metric(private val meter: Long) {

    fun toMeter(): Long = meter

    fun toKiloMeter(): Double = meter.toDouble() / 1000.0

    val kiloMeterPart = meter / 1000
    val meterPart = meter % 1000

    fun toShortenString(): String {
        return if (kiloMeterPart == 0L) meterPart.toString() + "m"
        else toKiloMeter().toString() + "km"
    }

    override fun toString(): String {
        return toShortenString()
    }

    companion object {
        fun fromMeter(meter: Long): Metric {
            return Metric(meter)
        }

        fun fromKiloMeter(kiloMeter: Long): Metric {
            return Metric(kiloMeter * 1000)
        }

        fun fromDistanceBetween(here: Location, there: Location): Metric {
            return Metric(here.distanceTo(there).toLong())
        }
    }
}