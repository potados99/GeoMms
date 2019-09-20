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

package com.potados.geomms.feature.conversations

import android.view.LayoutInflater
import android.view.ViewGroup
import com.potados.geomms.R
import com.potados.geomms.common.base.BaseRealmAdapter
import com.potados.geomms.common.base.BaseViewHolder
import com.potados.geomms.common.extension.setBold
import com.potados.geomms.common.extension.setTextColorRes
import com.potados.geomms.common.extension.setVisible
import com.potados.geomms.common.util.DateFormatter
import com.potados.geomms.model.Conversation
import io.realm.OrderedRealmCollection
import kotlinx.android.synthetic.main.conversation_list_item.view.*
import org.koin.core.KoinComponent
import org.koin.core.inject

class ConversationsAdapter : BaseRealmAdapter<Conversation>(), KoinComponent {

    private val dateFormatter: DateFormatter by inject()

    var onConversationClick: (Conversation) -> Unit = {}
    var onConversationLongClick: (Conversation) -> Unit = {}

    override fun updateData(data: OrderedRealmCollection<Conversation>?) {
        if (getData() === data) return

        super.updateData(data)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder {
        val view = LayoutInflater
            .from(parent.context)
            .inflate(R.layout.conversation_list_item, parent, false)

        if (viewType == VIEW_TYPE_UNREAD) {
            view.apply {
                title.setBold(true)
                snippet.setBold(true)
                snippet.setTextColorRes(R.color.textPrimary)
                unread.setVisible(true)
                date.setBold(true)
                date.setTextColorRes(R.color.textPrimary)
            }
        }

        return BaseViewHolder(view).apply {
            view.setOnClickListener {
                val conversation = getItem(adapterPosition) ?: return@setOnClickListener
                onConversationClick(conversation)
            }
            view.setOnLongClickListener {
                val conversation = getItem(adapterPosition) ?: return@setOnLongClickListener false

                onConversationLongClick(conversation)
                true
            }
        }
    }

    override fun onBindViewHolder(holder: BaseViewHolder, position: Int) {
        val conversation = getItem(position) ?: return
        val view = holder.containerView

        view.apply {
            avatars.contacts = conversation.recipients
            title.collapseEnabled = conversation.recipients.isNotEmpty()
            title.text = conversation.getTitle()
            date.text = dateFormatter.getConversationTimestamp(conversation.date).takeIf { conversation.date != 0L }
            snippet.text = conversation.snippet
        }
    }

    override fun getItemViewType(position: Int): Int =
        when (getItem(position)?.read) {
            true -> VIEW_TYPE_READ
            else -> VIEW_TYPE_UNREAD
        }

    companion object {
        private const val VIEW_TYPE_READ = 1
        private const val VIEW_TYPE_UNREAD = 0
    }
}
