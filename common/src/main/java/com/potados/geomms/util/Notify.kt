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

import android.content.Context
import android.widget.Toast
import androidx.annotation.StringRes

/**
 * Toast의 wrapper입니다.
 * 객체를 만들어서 써도 되고, 그러지 않아도 됩니다.
 */
class Notify(private val context: Context?) {

    fun short(message: String?) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }
    fun short(@StringRes message: Int, vararg formatArgs: Any?) = short(context?.getString(message, *formatArgs))

    fun long(message: String?) {
        Toast.makeText(context, message, Toast.LENGTH_LONG).show()
    }
    fun long(@StringRes message: Int) = long(context?.getString(message))

    companion object {
        fun short(context: Context?, message: String) {
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
        }
        fun long(context: Context?, message: String) {
            Toast.makeText(context, message, Toast.LENGTH_LONG).show()
        }
    }
}