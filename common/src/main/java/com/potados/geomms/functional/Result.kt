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

package com.potados.geomms.functional

import timber.log.Timber

/**
 * A generic class that holds a value with its loading status.
 */
sealed class Result<out T> {

    data class Success<out T>(val data: T) : Result<T>()
    data class Error(val exception: Exception) : Result<Nothing>()

    val succeeded get() = this is Success && data != null

    fun <R> onSuccess(body: (T) -> R): Result<T> {
        if (this is Success && data != null) {
            body(data)
        }
        return this
    }

    fun <R> onError(body: (Exception) -> R): Result<T> {
        if (this is Error) {
            body(exception)
        }
        return this
    }

    fun <R> either(onSuccess: (T) -> R, onError: (Exception) -> R): R =
        when (this) {
            is Success -> onSuccess(data)
            is Error -> onError(exception)
        }

    override fun toString(): String {
        return when (this) {
            is Success<*> -> "Success[data=$data]"
            is Error -> "Error[exception=$exception]"
        }
    }

    companion object {
        fun <R> of(body: () -> R): Result<R> {
            return try {
                Success(body())
            } catch (e: Exception) {
                Timber.e(e)
                Error(e)
            }
        }
    }
}