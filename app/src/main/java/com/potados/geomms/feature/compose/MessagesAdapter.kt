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

import android.graphics.Typeface
import android.telephony.PhoneNumberUtils
import android.text.Spannable
import android.text.SpannableString
import android.text.style.StyleSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.potados.geomms.R
import com.potados.geomms.common.base.BaseRealmAdapter
import com.potados.geomms.common.base.BaseViewHolder
import com.potados.geomms.common.extension.forwardTouches
import com.potados.geomms.common.extension.isVisible
import com.potados.geomms.common.extension.setPadding
import com.potados.geomms.common.extension.setVisible
import com.potados.geomms.common.navigation.Navigator
import com.potados.geomms.common.util.DateFormatter
import com.potados.geomms.extension.dpToPx
import com.potados.geomms.extension.isImage
import com.potados.geomms.extension.isVCard
import com.potados.geomms.extension.isVideo
import com.potados.geomms.feature.compose.BubbleUtils.Companion.canGroup
import com.potados.geomms.feature.compose.BubbleUtils.Companion.getBubble
import com.potados.geomms.feature.compose.part.PartsAdapter
import com.potados.geomms.model.Conversation
import com.potados.geomms.model.Message
import com.potados.geomms.model.Recipient
import kotlinx.android.synthetic.main.message_list_item_in.view.*
import org.koin.core.KoinComponent
import org.koin.core.inject
import java.util.*
import java.util.concurrent.TimeUnit

class MessagesAdapter : BaseRealmAdapter<Message>(), KoinComponent {

    lateinit var conversation: Conversation

    private val dateFormatter: DateFormatter by inject()
    private val navigator: Navigator by inject()

    private val partsViewPool = RecyclerView.RecycledViewPool()
    private val contactCache = ContactCache()

    var onMessageClick: (Message) -> Boolean = { true }
    var onMessageLongClick: (Message) -> Unit = {}

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder {

        val layoutInflater = LayoutInflater.from(parent.context)

        val view = when (viewType) {
            VIEW_TYPE_MESSAGE_OUT -> {
                layoutInflater.inflate(R.layout.message_list_item_out, parent, false)
            }
            else -> {
                layoutInflater.inflate(R.layout.message_list_item_in, parent, false)
            }
        }

        view.attachments.adapter = PartsAdapter(context, navigator)
        view.attachments.setRecycledViewPool(partsViewPool)
        view.body.forwardTouches(view)

        return BaseViewHolder(view).apply {
            containerView.setOnClickListener {
                val message = getItem(adapterPosition) ?: return@setOnClickListener
                if (onMessageClick(message)) {
                    it.status.setVisible(!it.status.isVisible)
                }
            }
            containerView.setOnLongClickListener {
                val message = getItem(adapterPosition) ?: return@setOnLongClickListener false
                onMessageLongClick(message)
                true
            }
        }
    }

    override fun onBindViewHolder(holder: BaseViewHolder, position: Int) {
        val message = getItem(position) ?: return
        val previous = if (position == 0) null else getItem(position - 1)
        val next = if (position == itemCount - 1) null else getItem(position + 1)
        val view = holder.containerView

        // Bind the status
        bindStatus(holder, message, next)

        val timeSincePrevious = TimeUnit.MILLISECONDS.toMinutes(message.date - (previous?.date ?: 0))

        val timeText = dateFormatter.getMessageTimestamp(message.date)
        val timeVisibility = (timeSincePrevious >= BubbleUtils.TIMESTAMP_THRESHOLD)

        view.timestamp.text = timeText
        view.timestamp.setVisible(timeVisibility)

        // Bind the grouping
        val media = message.parts.filter { it.isImage() || it.isVideo() || it.isVCard() }
        view.setPadding(bottom = if (canGroup(message, next)) 0 else 16.dpToPx(context))

        // Bind the avatar
        if (!message.isMe()) {
            view.avatar.setContact(contactCache[message.address])
            view.avatar.setVisible(!canGroup(message, next), View.INVISIBLE)
        }

        // Bind the body text
        view.body.text = when (message.isSms()) {
            true -> message.body
            false -> {
                val subject = message.getCleansedSubject()
                val body = message.parts
                    .filter { part -> !part.isVCard() }
                    .mapNotNull { part -> part.text }
                    .filter { part -> part.isNotBlank() }
                    .joinToString("\n")

                when {
                    subject.isNotBlank() -> {
                        val spannable = SpannableString(if (body.isNotBlank()) "$subject\n$body" else subject)
                        spannable.setSpan(StyleSpan(Typeface.BOLD), 0, subject.length, Spannable.SPAN_INCLUSIVE_EXCLUSIVE)
                        spannable
                    }
                    else -> body
                }
            }
        }
        view.body.setVisible(message.isSms() || view.body.text.isNotBlank())
        view.body.setBackgroundResource(getBubble(
            canGroupWithPrevious = canGroup(message, previous) || media.isNotEmpty(),
            canGroupWithNext = canGroup(message, next),
            isMe = message.isMe()))

        // Bind the attachments
        val partsAdapter = view.attachments.adapter as PartsAdapter
        partsAdapter.setData(message, previous, next, view)
    }

    override fun getItemViewType(position: Int): Int {
        val message = getItem(position) ?: return -1
        return when (message.isMe()) {
            true -> VIEW_TYPE_MESSAGE_OUT   // send
            false -> VIEW_TYPE_MESSAGE_IN   // receive
        }
    }

    private fun bindStatus(viewHolder: BaseViewHolder, message: Message, next: Message?) {
        val view = viewHolder.containerView

        val age = TimeUnit.MILLISECONDS.toMinutes(System.currentTimeMillis() - message.date)

        view.status.text = when {
            message.isSending() -> context.getString(R.string.message_status_sending)
            message.isDelivered() -> context.getString(R.string.message_status_delivered, dateFormatter.getTimestamp(message.dateSent))
            message.isFailedMessage() -> context.getString(R.string.message_status_failed)

            // Incoming group message
            !message.isMe() && conversation.recipients.size > 1 -> {
                "${contactCache[message.address]?.getDisplayName()} • ${dateFormatter.getTimestamp(message.date)}"
            }

            else -> dateFormatter.getTimestamp(message.date)
        }

        view.status.setVisible(when {
            message.isSending() -> true
            message.isFailedMessage() -> true
            message.isDelivered() && next?.isDelivered() != true && age <= BubbleUtils.TIMESTAMP_THRESHOLD -> true
            else -> false
        })
    }

    /**
     * Cache the contacts in a map by the address, because the messages we're binding don't have
     * a reference to the contact.
     */
    private inner class ContactCache : HashMap<String, Recipient?>() {

        override fun get(key: String): Recipient? {
            if (super.get(key)?.isValid != true) {
                set(key, conversation.recipients.firstOrNull { PhoneNumberUtils.compare(it.address, key) })
            }

            return super.get(key)?.takeIf { it.isValid }
        }

    }


    companion object {
        private const val VIEW_TYPE_MESSAGE_IN = 0
        private const val VIEW_TYPE_MESSAGE_OUT = 1

    }
}