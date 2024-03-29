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

package com.potados.geomms.manager

/**
 * Helper class for generating incrementing ids for messages
 */
abstract class KeyManager : Manager() {

    /**
     * Should be called when a new sync is being started
     */
    abstract fun reset(channel: Int)

    /**
     * Returns a valid ID
     */
    abstract fun newId(channel: Int): Long

    /**
     * Returns random ID
     */
    abstract fun randomId(max: Long): Long
}