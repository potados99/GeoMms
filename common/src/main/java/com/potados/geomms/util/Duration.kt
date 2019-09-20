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

import java.util.concurrent.TimeUnit
import kotlin.math.abs

/**
 * 시간 간격을 나타냅니다.

 * @param millis:               밀리초로 나타낸 시간 간격.
 * @throws RuntimeException:    millis가 음수일 때.
 */
class Duration(private val millis: Long) {

    init {
        if (millis < 0) {
            throw RuntimeException("negative duration cannot exist.")
        }
    }

    /**
     * Duration을 밀리초로 표현합니다.
     */
    fun toMillis(): Long {
        return millis
    }

    fun toSeconds(): Long {
        return TimeUnit.MILLISECONDS.toSeconds(millis)
    }

    /**
     * Duration을 분으로 표현합니다.
     */
    fun toMinutes(): Long {
        return TimeUnit.MILLISECONDS.toMinutes(millis)
    }

    /**
     * Duration을 시간으로 표현합니다.
     */
    fun toHours(): Long {
        return TimeUnit.MILLISECONDS.toHours(millis)
    }

    /**
     * Duration을 분, 초, 밀리초로 나누어 표현합니다.
     */
    val minutePart = (millis / 1000) / 60
    val secondPart = (millis / 1000) % 60
    val millisPart = millis % 1000


    fun toShortenString(): String {
        return if (minutePart == 0L) secondPart.toString() + "second(s)"
        else minutePart.toString() + "minute(s)"
    }

    override fun toString(): String {
        return toShortenString()
    }


    companion object {
        fun between(some: DateTime, another: DateTime): Duration {
            return Duration(abs(another.timeStamp - some.timeStamp))
        }
    }

}