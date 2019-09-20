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

import java.io.Serializable
import java.text.SimpleDateFormat
import java.util.*

class DateTime(val timeStamp: Long) : Serializable {

    private val date = Date(timeStamp)

    fun toString(format: String): String {
        return SimpleDateFormat(format, Locale.getDefault()).format(date)
    }

    override fun toString(): String {
        return toString("yyyy-MM-dd hh:mm:ss")
    }

    fun toShortenString(): String {
        val nowDate = Date()
        val now = Calendar.getInstance().apply {
            time = nowDate
        }

        val thenDate = date
        val then = Calendar.getInstance().apply {
            time = thenDate
        }

        val deltaDate = now.get(Calendar.DATE) - then.get(Calendar.DATE)

        if (deltaDate > 6) {
            /**
             * 일주일 지남
             */

            return SimpleDateFormat("MM/dd/yy", Locale.KOREA).format(thenDate)
        }
        else if (deltaDate > 1) {
            /**
             * 하루 초과 지남
             */

            return SimpleDateFormat("E", Locale.KOREA).format(thenDate)
        }
        else if (deltaDate > 0) {
            /**
             * 하루 지남
             */

            return "Yesterday"
        }
        else {
            /**
             * 아직 당일임
             */

            return SimpleDateFormat("HH:mm", Locale.KOREA).format(thenDate)
        }
    }

    fun durationUntil(then: DateTime): Duration {
        return Duration.between(then, this)
    }

    fun durationUntilNow(): Duration {
        return Duration.between(now(), this)
    }

    companion object {
        fun getCurrentTimeStamp(): Long {
            return System.currentTimeMillis()
        }

        fun now(): DateTime {
            return DateTime(getCurrentTimeStamp())
        }
    }
}