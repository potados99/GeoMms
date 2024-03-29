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

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.potados.geomms.common.base.BaseAdapter
import com.potados.geomms.common.base.BaseViewHolder
import com.potados.geomms.common.extension.forwardTouches
import com.potados.geomms.common.navigation.Navigator
import com.potados.geomms.extension.isImage
import com.potados.geomms.extension.isVCard
import com.potados.geomms.extension.isVideo
import com.potados.geomms.feature.compose.BubbleUtils.Companion.canGroup
import com.potados.geomms.model.Message
import com.potados.geomms.model.MmsPart
import kotlinx.android.synthetic.main.message_list_item_in.view.*

class PartsAdapter(context: Context, navigator: Navigator) : BaseAdapter<MmsPart>() {

    private val partBinders = listOf(
            MediaBinder(context, navigator),
            VCardBinder(context, navigator)
    )

    private lateinit var message: Message
    private var previous: Message? = null
    private var next: Message? = null
    private var messageView: View? = null
    private var bodyVisible: Boolean = true

    fun setData(message: Message, previous: Message?, next: Message?, messageView: View) {
        this.message = message
        this.previous = previous
        this.next = next
        this.messageView = messageView
        this.bodyVisible = messageView.body.visibility == View.VISIBLE
        this.data = message.parts.filter { it.isImage() || it.isVideo() || it.isVCard() }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder {
        val layout = partBinders.getOrNull(viewType)?.partLayout ?: 0
        val view = LayoutInflater.from(parent.context).inflate(layout, parent, false)
        messageView?.let(view::forwardTouches)
        return BaseViewHolder(view)
    }

    override fun onBindViewHolder(holder: BaseViewHolder, position: Int) {
        val part = data[position]
        val view = holder.containerView

        val canGroupWithPrevious = canGroup(message, previous) || position > 0
        val canGroupWithNext = canGroup(message, next) || position < itemCount - 1 || bodyVisible

        partBinders
                .firstOrNull { it.canBindPart(part) }
                ?.bindPart(view, part, message, canGroupWithPrevious, canGroupWithNext)
    }

    override fun getItemViewType(position: Int): Int {
        val part = data[position]
        return partBinders.indexOfFirst { it.canBindPart(part) }
    }

}