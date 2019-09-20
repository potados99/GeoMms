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

package com.potados.geomms.common.widget

import android.content.Context
import android.util.AttributeSet
import android.view.View
import androidx.constraintlayout.widget.ConstraintLayout
import com.potados.geomms.R
import com.potados.geomms.model.Recipient
import kotlinx.android.synthetic.main.group_avatar_view.view.*

class GroupAvatarView(context: Context, attrs: AttributeSet? = null) : ConstraintLayout(context, attrs) {

    var contacts: List<Recipient> = ArrayList()
        set(value) {
            field = value
            updateView()
        }

    private val avatars by lazy { listOf(avatar1, avatar2, avatar3) }

    init {
        View.inflate(context, R.layout.group_avatar_view, this)
        setBackgroundResource(R.drawable.circle)
        clipToOutline = true
    }

    override fun onFinishInflate() {
        super.onFinishInflate()

        avatars.forEach { avatar ->
            avatar.setBackgroundResource(R.drawable.rectangle)
        }

        if (!isInEditMode) {
            updateView()
        }
    }

    private fun updateView() {
        avatars.forEachIndexed { index, avatar ->
            avatar.visibility = if (contacts.size > index) View.VISIBLE else View.GONE
            avatar.setContact(contacts.getOrNull(index))
        }
    }

}