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


package com.potados.geomms.model

import android.content.Context
import android.net.Uri
import android.os.Build
import androidx.core.view.inputmethod.InputContentInfoCompat

sealed class Attachment {

    data class Image(
        private val uri: Uri? = null,
        private val inputContent: InputContentInfoCompat? = null
    ) : Attachment() {

        fun getUri(): Uri? {
            return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N_MR1) {
                inputContent?.contentUri ?: uri
            } else {
                uri
            }
        }

        fun isGif(context: Context): Boolean {
            return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N_MR1 && inputContent != null) {
                inputContent.description.hasMimeType("image/gif")
            } else {
                if (uri == null) false
                else context.contentResolver.getType(uri) == "image/gif"
            }
        }
    }

    data class Contact(val vCard: String) : Attachment()
}

class Attachments(attachments: List<Attachment>) : List<Attachment> by attachments
