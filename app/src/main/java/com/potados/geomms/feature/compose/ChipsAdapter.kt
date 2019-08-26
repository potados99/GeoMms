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
package com.potados.geomms.feature.compose

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.recyclerview.widget.RecyclerView
import com.google.android.flexbox.FlexboxLayoutManager
import com.potados.geomms.R
import com.potados.geomms.common.base.BaseAdapter
import com.potados.geomms.common.base.BaseViewHolder
import com.potados.geomms.extension.dpToPx
import com.potados.geomms.common.extension.resolveThemeColor
import com.potados.geomms.common.extension.showKeyboard
import com.potados.geomms.model.Contact
import kotlinx.android.synthetic.main.contact_chip.view.*

class ChipsAdapter(private val context: Context) : BaseAdapter<Contact>() {

    companion object {
        private const val TYPE_EDIT_TEXT = 0
        private const val TYPE_ITEM = 1
    }

    private val hint: String = context.getString(R.string.title_compose)

    val editText = View.inflate(context, R.layout.chip_input_list_item, null) as EditText

    var view: RecyclerView? = null

    init {
        val wrap = ViewGroup.LayoutParams.WRAP_CONTENT
        editText.layoutParams = FlexboxLayoutManager.LayoutParams(wrap, wrap).apply {
            minHeight = 36.dpToPx(context)
            minWidth = 56.dpToPx(context)
            flexGrow = 8f
        }

        editText.hint = hint
    }

    override fun onDatasetChanged() {
        editText.text = null
        editText.hint = if (itemCount == 1) hint else null

        if (itemCount != 2) {
            editText.showKeyboard()
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = when (viewType) {
        TYPE_EDIT_TEXT -> {
            editText.setTextColor(parent.context.resolveThemeColor(android.R.attr.textColorPrimary))
            editText.setHintTextColor(parent.context.resolveThemeColor(android.R.attr.textColorTertiary))
            BaseViewHolder(editText)
        }

        else -> {
            val inflater = LayoutInflater.from(parent.context)
            val view = inflater.inflate(R.layout.contact_chip, parent, false)
            BaseViewHolder(view)
        }
    }

    override fun onBindViewHolder(holder: BaseViewHolder, position: Int) {
        when (getItemViewType(position)) {
            TYPE_ITEM -> {
                val contact = getItem(position)
                val view = holder.containerView

                view.avatar.setContact(contact)

                // If the contact's name is empty, try to display a phone number instead
                // The contacts provided here should only have one number
                view.name.text = if (contact.name.isNotBlank()) {
                    contact.name
                } else {
                    contact.numbers.firstOrNull { it.address.isNotBlank() }?.address ?: ""
                }
            }
        }
    }

    override fun getItemCount() = super.getItemCount() + 1

    override fun getItemViewType(position: Int) = if (position == itemCount - 1) TYPE_EDIT_TEXT else TYPE_ITEM

    override fun areItemsTheSame(old: Contact, new: Contact): Boolean {
        return old.lookupKey == new.lookupKey
    }
}
