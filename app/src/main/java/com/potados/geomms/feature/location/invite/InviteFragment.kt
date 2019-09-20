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

package com.potados.geomms.feature.location.invite

import android.content.Intent
import android.os.Bundle
import android.telephony.PhoneNumberUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.potados.geomms.R
import com.potados.geomms.common.base.BaseFragment
import com.potados.geomms.common.extension.getViewModel
import com.potados.geomms.common.extension.isVisible
import com.potados.geomms.common.extension.setOnTextChanged
import com.potados.geomms.common.extension.setSupportActionBar
import com.potados.geomms.databinding.InviteFragmentBinding
import com.potados.geomms.feature.compose.ChipsAdapter
import com.potados.geomms.feature.compose.ContactAdapter
import com.potados.geomms.feature.location.MapFragment.Companion.ACTION_SET_ADDRESS
import com.potados.geomms.feature.location.MapFragment.Companion.EXTRA_ADDRESS
import com.potados.geomms.model.Contact
import com.potados.geomms.model.PhoneNumber
import io.realm.RealmList
import kotlinx.android.synthetic.main.invite_fragment.view.*
import org.koin.core.KoinComponent
import java.util.*

class InviteFragment : BaseFragment(), KoinComponent {

    private lateinit var inviteViewModel: InviteViewModel
    private lateinit var viewDataBinding: InviteFragmentBinding

    private val chipsAdapter = ChipsAdapter()
    private val recentAdapter = ContactAdapter()
    private val contactAdapter = ContactAdapter()

    init {
        failables += this
        failables += chipsAdapter
        failables += recentAdapter
        failables += contactAdapter
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        inviteViewModel = getViewModel()
        failables += inviteViewModel.failables
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return InviteFragmentBinding
            .inflate(inflater, container, false)
            .apply { vm = inviteViewModel }
            .apply { lifecycleOwner = this@InviteFragment }
            .apply { viewDataBinding = this }
            .apply { setSupportActionBar(toolbar = root.toolbar, title = false, upButton = true) }
            .apply { initializeView(root) }
            .root
    }

    private fun initializeView(view: View) {

        with(view.chips) {
            adapter = chipsAdapter.apply {
                editText.setOnTextChanged(inviteViewModel::onSearch)
                editText.requestFocus()
            }
        }

        with(view.recents) {
            adapter = recentAdapter.apply {
                companionView = view.recent_group
                onContactClick = { inviteViewModel.onContactClick(activity, it) }
            }
        }

        with(view.contacts) {
            adapter = contactAdapter.apply {
                onContactClick = { inviteViewModel.onContactClick(activity, it) }
            }
        }
    }
}