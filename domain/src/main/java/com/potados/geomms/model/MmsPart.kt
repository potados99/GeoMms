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

package com.potados.geomms.model

import androidx.core.net.toUri
import io.realm.RealmObject
import io.realm.RealmResults
import io.realm.annotations.LinkingObjects
import io.realm.annotations.PrimaryKey

open class MmsPart : RealmObject() {

    @PrimaryKey
    var id: Long = 0
    var type: String = ""
    var text: String? = null

    @LinkingObjects("parts") /* Message::parts */
    val messages: RealmResults<Message>? = null

    fun getUri() = "content://mms/part/$id".toUri()

    fun getSummary(): String? = when {
        type == "text/plain" -> text
        type == "text/x-vCard" -> "Contact card"
        type.startsWith("image") -> "Photo"
        type.startsWith("video") -> "Video"
        else -> null
    }}