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

package com.potados.geomms.feature.compose.part

import android.content.ContentUris
import android.content.Context
import android.view.View
import com.potados.geomms.R
import com.potados.geomms.common.navigation.Navigator
import com.potados.geomms.extension.isVCard
import com.potados.geomms.feature.compose.BubbleUtils
import com.potados.geomms.mapper.CursorToPartImpl
import com.potados.geomms.model.Message
import com.potados.geomms.model.MmsPart
import ezvcard.Ezvcard
import kotlinx.android.synthetic.main.mms_vcard_list_item.view.*

class VCardBinder(
    private val context: Context,
    private val navigator: Navigator
) : PartBinder {

    override val partLayout = R.layout.mms_vcard_list_item

    override fun canBindPart(part: MmsPart) = part.isVCard()

    override fun bindPart(
        view: View,
        part: MmsPart,
        message: Message,
        canGroupWithPrevious: Boolean,
        canGroupWithNext: Boolean
    ) {
        val uri = ContentUris.withAppendedId(CursorToPartImpl.CONTENT_URI, part.id)
        val bubble = BubbleUtils.getBubble(canGroupWithPrevious, canGroupWithNext, message.isMe())

        view.setOnClickListener { navigator.saveVcard(uri) }
        view.vcard_background.setBackgroundResource(bubble)

        // TODO: do on background
        context.contentResolver.openInputStream(uri)
            .use { Ezvcard.parse(it).first() }
            .let { view.name?.text = it.formattedName.value }

    }

}