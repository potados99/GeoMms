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

package com.potados.geomms.common.widget

import android.content.Context
import android.graphics.Color
import android.telephony.PhoneNumberUtils
import android.util.AttributeSet
import android.view.View
import android.widget.FrameLayout
import com.bumptech.glide.Glide
import com.potados.geomms.R
import com.potados.geomms.common.extension.resolveThemeColor
import com.potados.geomms.common.extension.setBackgroundTint
import com.potados.geomms.common.extension.setTint
import com.potados.geomms.model.Contact
import com.potados.geomms.model.Recipient
import kotlinx.android.synthetic.main.avatar_view.view.*

class AvatarView(context: Context, attrs: AttributeSet? = null) : FrameLayout(context, attrs) {

    private var lookupKey: String? = null
    private var name: String? = null
    private var address: String? = null

    init {
        View.inflate(context, R.layout.avatar_view, this)

        setBackgroundResource(R.drawable.circle)
        clipToOutline = true
    }

    override fun onFinishInflate() {
        super.onFinishInflate()

        if (!isInEditMode) {
            setBackgroundTint(context.resolveThemeColor(R.attr.tintPrimary))
            updateView()
        }
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
            // TODO
            // Glide.with(photo).load(PhoneNumberUtils.stripSeparators(address)).into(photo)
        }

    }
}