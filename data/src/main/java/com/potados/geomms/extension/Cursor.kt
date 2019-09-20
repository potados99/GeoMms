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

import android.database.Cursor

fun Cursor.forEach(closeOnComplete: Boolean = true, method: (Cursor) -> Unit = {}) {
    moveToPosition(-1)
    while (moveToNext()) {
        method.invoke(this)
    }

    if (closeOnComplete) {
        close()
    }
}

fun <T> Cursor.map(map: (Cursor) -> T): List<T> {
    return List(count) { position ->
        moveToPosition(position)
        map(this)
    }
}

fun <T> Cursor.mapWhile(map: (Cursor) -> T, predicate: (T) -> Boolean): ArrayList<T> {
    val result = ArrayList<T>()

    moveToPosition(-1)
    while (moveToNext()) {
        val item = map(this)

        if (!predicate(item)) break

        result.add(item)
    }

    return result
}




