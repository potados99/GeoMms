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

import android.view.LayoutInflater
import android.view.ViewGroup
import com.potados.geomms.R
import com.potados.geomms.common.base.BaseAdapter
import com.potados.geomms.common.base.BaseViewHolder
import com.potados.geomms.model.Contact
import com.potados.geomms.model.PhoneNumber
import kotlinx.android.synthetic.main.contact_list_item.view.*

class PhoneNumberAdapter(
    private val onNumberClick: (Contact, Int) -> Unit
) : BaseAdapter<PhoneNumber>() {

    lateinit var contact: Contact

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val view = inflater.inflate(R.layout.contact_number_list_item, parent, false)
        return BaseViewHolder(view)
    }

    override fun onBindViewHolder(holder: BaseViewHolder, position: Int) {
        val number = getItem(position)
        val view = holder.containerView

        // Setting this in onCreateViewHolder causes a crash sometimes. [contact] returns the
        // contact from a different row, I'm not sure why
        view.setOnClickListener { onNumberClick(contact, position) }

        view.address.text = number.address
        view.type.text = number.type
    }

}