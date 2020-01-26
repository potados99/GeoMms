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
package com.potados.geomms.feature.compose

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import com.bumptech.glide.Glide
import com.potados.geomms.R
import com.potados.geomms.common.base.BaseAdapter
import com.potados.geomms.common.base.BaseViewHolder
import com.potados.geomms.model.Attachment
import kotlinx.android.synthetic.main.attachment_image_list_item.view.*

class AttachmentAdapter(private val context: Context) : BaseAdapter<Attachment>() {

    var onDetach: (Attachment) -> Unit = {}

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val view = when (viewType) {
            VIEW_TYPE_IMAGE -> inflater.inflate(R.layout.attachment_image_list_item, parent, false)
                    .apply { thumbnail_bounds.clipToOutline = true }

            else -> null!! // Impossible
        }

        return BaseViewHolder(view).apply {
            view.detach.setOnClickListener {
                val attachment = getItem(adapterPosition) ?: return@setOnClickListener
                onDetach(attachment)
            }
        }
    }

    override fun onBindViewHolder(holder: BaseViewHolder, position: Int) {
        val attachment = getItem(position)
        val view = holder.containerView

        when (attachment) {
            is Attachment.Image -> Glide.with(context)
                    .load(attachment.getUri())
                    .into(view.thumbnail)
        }
    }

    companion object {
        private const val VIEW_TYPE_IMAGE = 0
    }
}