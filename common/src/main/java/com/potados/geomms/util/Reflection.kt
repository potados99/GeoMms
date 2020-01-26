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

package com.potados.geomms.util

import kotlin.reflect.KProperty1
import kotlin.reflect.full.memberProperties

class Reflection {
    companion object {
        /**
         * https://stackoverflow.com/a/35539628
         * 땡큐
         */
        @Suppress("UNCHECKED_CAST")
        fun <R> readInstanceProperty(instance: Any, propertyName: String): R {
            val property = instance::class.memberProperties
                .first { it.name == propertyName } as KProperty1<Any, *>

            return property.get(instance) as R
        }
    }
}