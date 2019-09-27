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

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.potados.geomms.common.base.BaseFragment
import com.potados.geomms.common.extension.getViewModel
import com.potados.geomms.common.extension.observe
import com.potados.geomms.common.extension.setVisible
import com.potados.geomms.databinding.ConnectionDetailFragmentBinding
import kotlinx.android.synthetic.main.connection_detail_fragment.view.*

class ConnectionDetailFragment : BaseFragment() {

    private lateinit var viewDataBinding: ConnectionDetailFragmentBinding
    private lateinit var connectionDetailViewModel: ConnectionDetailViewModel

    init {
        failables += this
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        connectionDetailViewModel = getViewModel {
            startWithArguments(this@ConnectionDetailFragment, arguments)
            observe(positiveButtonAlpha) {
                it?.let {
                    with(viewDataBinding.positive) {
                        isClickable = it > 0.6f
                        alpha = it
                    }
                    viewDataBinding.positive.alpha = it
                }
            }
        }
        failables += connectionDetailViewModel.failables
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return ConnectionDetailFragmentBinding
            .inflate(inflater)
            .apply { lifecycleOwner = this@ConnectionDetailFragment }
            .apply { vm = connectionDetailViewModel }
            .apply { viewDataBinding = this }
            .apply { initializeView(root) }
            .root
    }

    private fun initializeView(view: View) {
        with(view.negative) {
            setOnClickListener { connectionDetailViewModel.onNegativeButton(this@ConnectionDetailFragment) }
        }

        with(view.positive) {
            setOnClickListener { connectionDetailViewModel.onPositiveButton(this@ConnectionDetailFragment) }
        }
    }

    companion object {
        const val ARG_CONNECTION_ID = "connection_id"

        fun ofConnection(connectionId: Long): ConnectionDetailFragment {
            return ConnectionDetailFragment().apply {
                arguments = Bundle().apply {
                    putLong(ARG_CONNECTION_ID, connectionId)
                }
            }
        }
    }
}