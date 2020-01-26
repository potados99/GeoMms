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

package com.potados.geomms.common.extension

import android.content.Context
import android.view.Menu
import android.view.MenuItem


/** Returns a [MutableIterator] over the items in this menu. */
operator fun Menu.iterator() = object : MutableIterator<MenuItem> {
    private var index = 0
    override fun hasNext() = index < size()
    override fun next() = getItem(index++) ?: throw IndexOutOfBoundsException()
    override fun remove() = removeItem(--index)
}

fun Menu.setTint(context: Context?, color: Int) {
    context?.let {
        iterator().forEach { item ->
            item.icon?.setTint(color)
        }
    }
}

fun Menu.setVisible(visible: Boolean) {
    iterator().forEach {
        it.isVisible = visible
    }
}