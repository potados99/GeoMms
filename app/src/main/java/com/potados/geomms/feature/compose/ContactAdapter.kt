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

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.potados.geomms.R
import com.potados.geomms.common.base.BaseAdapter
import com.potados.geomms.common.base.BaseViewHolder
import com.potados.geomms.common.extension.isVisible
import com.potados.geomms.common.extension.setVisible
import com.potados.geomms.model.Contact
import kotlinx.android.synthetic.main.contact_list_item.view.*
import kotlin.math.min

class ContactAdapter : BaseAdapter<Contact>() {

    private val numbersViewPool = RecyclerView.RecycledViewPool()

    var onContactClick: (Contact) -> Unit = {}

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val view = layoutInflater.inflate(R.layout.contact_list_item, parent, false)

        view.addresses.setRecycledViewPool(numbersViewPool)

        return BaseViewHolder(view).apply {
            view.primary.setOnClickListener {
                val contact = getItem(adapterPosition) ?: return@setOnClickListener
                onContactClick(copyContact(contact, 0))
            }

            view.addresses.adapter = PhoneNumberAdapter { contact, index ->
                onContactClick(copyContact(contact, index + 1))
            }
        }
    }

    override fun onBindViewHolder(holder: BaseViewHolder, position: Int) {
        val view = holder.containerView
        val item = getItem(position) ?: return
        val next = getItem(position + 1)

        val isRecent = item.lastConnected != 0L
        val isNextRecent = next?.lastConnected != 0L
        val isFirstRecent = isRecent && position == 0
        val isLastRecent = isRecent && !isNextRecent

        view.avatar.setContact(item)
        view.name.text = item.name
        view.name.setVisible(view.name.text.isNotEmpty())
        view.address.text = item.numbers.firstOrNull()?.address ?: ""
        view.type.text = item.numbers.firstOrNull()?.type ?: ""

        val adapter = view.addresses.adapter as PhoneNumberAdapter
        adapter.contact = item
        adapter.data = item.numbers.drop(min(item.numbers.size, 1))

        // Distinct recent contacts.
        view.recent_title.isVisible = isFirstRecent
        view.recent_separator.isVisible = isLastRecent
    }

    /**
     * Creates a copy of the contact with only one phone number, so that the chips
     * sheetView can still display thepackageName/photo, and not get confused about which phone number to use
     */
    private fun copyContact(contact: Contact, numberIndex: Int) = Contact().apply {
        lookupKey = contact.lookupKey
        name = contact.name
        numbers.add(contact.numbers[numberIndex])
    }

    override fun areItemsTheSame(old: Contact, new: Contact): Boolean {
        return old.lookupKey == new.lookupKey
    }
}