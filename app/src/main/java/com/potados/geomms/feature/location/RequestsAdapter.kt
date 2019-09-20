/*
 * Copyright (C) 2019 Song Byeong Jun <potados99@gmail.com>
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

package com.potados.geomms.feature.location

import android.view.LayoutInflater
import android.view.ViewGroup

import com.potados.geomms.R
import com.potados.geomms.common.base.BaseRealmAdapter
import com.potados.geomms.common.base.BaseViewHolder
import com.potados.geomms.model.ConnectionRequest
import kotlinx.android.synthetic.main.request_list_item.view.*
import org.koin.core.KoinComponent

class RequestsAdapter : BaseRealmAdapter<ConnectionRequest>(), KoinComponent {

    var onRequestClick: (ConnectionRequest) -> Unit = {}
    var onRequestLongClick: (ConnectionRequest) -> Unit = {}

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.request_list_item, parent, false)

        return BaseViewHolder(view).apply {
            view.setOnClickListener {
                val item = getItem(adapterPosition) ?: return@setOnClickListener
                onRequestClick(item)
            }
            view.setOnLongClickListener {
                val item = getItem(adapterPosition) ?: return@setOnLongClickListener false
                onRequestLongClick(item)
                true
            }
        }
    }

    override fun onBindViewHolder(holder: BaseViewHolder, position: Int) {
        val connection = getItem(position) ?: return
        val view = holder.containerView

        view.name.text = connection.recipient?.getDisplayName()
        view.avatar.setContact(connection.recipient)
    }

    interface RequestClickListener {
        fun onRequestClick(request: ConnectionRequest)
        fun onRequestLongClick(request: ConnectionRequest)
    }

}
