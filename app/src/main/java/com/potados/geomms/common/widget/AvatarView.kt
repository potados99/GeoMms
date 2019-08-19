/*
 * Copyright (C) 2017 Moez Bhatti <moez.bhatti@gmail.com>
 *
 * This file is part of QKSMS.
 *
 * QKSMS is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * QKSMS is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with QKSMS.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.potados.geomms.common.widget

import android.content.Context
import android.graphics.Color
import android.telephony.PhoneNumberUtils
import android.util.AttributeSet
import android.view.View
import android.widget.FrameLayout
import com.bumptech.glide.Glide
import com.potados.geomms.R
import com.potados.geomms.common.extension.*
import com.potados.geomms.model.Contact
import com.potados.geomms.model.Recipient
import kotlinx.android.synthetic.main.avatar_view.view.*

class AvatarView(context: Context, attrs: AttributeSet? = null) : FrameLayout(context, attrs) {

    /**
     * This value can be changes if we should use the theme from a particular conversation
     */
    var threadId: Long = 0
        set(value) {
            if (field == value) return
            field = value
            applyTheme(value)
        }

    private var lookupKey: String? = null
    private var name: String? = null
    private var address: String? = null

    init {
        View.inflate(context, R.layout.avatar_view, this)

        setBackgroundResource(R.drawable.circle)
        clipToOutline = true
    }

    /**
     * If the [recipient] has a contact: use the contact's avatar, but keep the address.
     * Use the recipient address otherwise.
     */
    fun setContact(recipient: Recipient?) {
        // If the recipient has a contact, just use that and return
        recipient?.contact?.let { contact ->
            setContact(contact, recipient.address)
            return
        }

        lookupKey = null
        name = null
        address = recipient?.address
        updateView()
    }

    /**
     * Use the [contact] information to display the avatar.
     * A specific [contactAddress] can be specified (useful when the contact has several addresses).
     */
    fun setContact(contact: Contact?, contactAddress: String? = null) {
        lookupKey = contact?.lookupKey
        name = contact?.name
        // If a contactAddress has been given, we use it. Use the contact address otherwise.
        address = contactAddress ?: contact?.numbers?.firstOrNull()?.address
        updateView()
    }

    override fun onFinishInflate() {
        super.onFinishInflate()

        if (!isInEditMode) {
            applyTheme(threadId)
            updateView()
        }
    }

    fun applyTheme(threadId: Long) {
        setBackgroundTint(context.resolveThemeColor(R.attr.avatarColor))
        initial.setTextColor(Color.WHITE)
        icon.setTint(Color.WHITE)
    }

    private fun updateView() {
        if (name?.isNotEmpty() == true) {
            initial.text = name?.substring(0, 1)
            icon.visibility = GONE
        } else {
            initial.text = null
            icon.visibility = VISIBLE
        }

        photo.setImageDrawable(null)
        address?.let { address ->
            // Glide.with(photo).load(PhoneNumberUtils.stripSeparators(address)).into(photo)
        }

    }
}