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

package com.potados.geomms.extension

import com.potados.geomms.base.Failable
import timber.log.Timber

/**
 * Run code block.
 * Set failure if any exceptions are thrown.
 *
 * @return null if any exceptions.
 */
fun <T> Failable.nullOnFail(logOnError: Boolean = true, body: () -> T?): T? {
    return try {
        body()
    } catch (e: Exception) {
        if (logOnError) {
            Timber.w(e)
        }
        setFailure(Failable.Failure(e.message ?: "Unknown failure"))

        null
    }
}

/**
 * Run code block.
 * Set failure if any exceptions.
 */
fun <T> Failable.unitOnFail(logOnError: Boolean = true, body: () -> T?) {
    try {
        body()
    } catch (e: Exception) {
        if (logOnError) {
            Timber.w(e)
        }
        setFailure(Failable.Failure(e.message ?: "Unknown failure"))
    }
}

/**
 * Run code block.
 * Set failure if any exceptions are thrown.
 *
 * @return false if any exceptions.
 */
fun Failable.falseOnFail(logOnError: Boolean = true, body: () -> Boolean): Boolean {
    return try {
        body()
    } catch (e: Exception) {
        if (logOnError) {
            Timber.w(e)
        }
        setFailure(Failable.Failure(e.message ?: "Unknown failure"))

        false
    }
}
