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

package com.potados.geomms.feature.location

import android.animation.LayoutTransition
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.maps.GoogleMap
import com.potados.geomms.R
import com.potados.geomms.common.base.BaseFragment
import com.potados.geomms.common.extension.*
import com.potados.geomms.common.widget.CustomBottomSheetBehavior
import com.potados.geomms.databinding.ConnectionsFragmentBinding
import com.potados.geomms.extension.withNonNull
import com.potados.geomms.model.Connection
import com.potados.geomms.model.ConnectionRequest
import com.potados.geomms.util.Notify
import com.potados.geomms.util.Popup
import kotlinx.android.synthetic.main.connections_fragment.*
import kotlinx.android.synthetic.main.connections_fragment.view.*

class ConnectionsFragment : BaseFragment() {

    private lateinit var connectionsViewModel: ConnectionsViewModel
    private lateinit var viewDataBinding: ConnectionsFragmentBinding

    private var connectionsAdapter = ConnectionsAdapter()
    private val requestsAdapter = RequestsAdapter()

    init {
        failables += this
        failables += connectionsAdapter
        failables += requestsAdapter
    }

    /**
     * Propagate actions to outside
     * Place this here, not in the sheetView model.
     */
    var onShowConnectionOnMap: (connection: Connection) -> Unit = {}

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        connectionsViewModel = getViewModel()
        failables += connectionsViewModel
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return ConnectionsFragmentBinding.inflate(inflater)
            .apply { vm = connectionsViewModel }
            .apply { lifecycleOwner = this@ConnectionsFragment }
            .apply { viewDataBinding = this }
            .apply { initializeView(root) }
            .root
    }

    private fun initializeView(view: View) {

        with(view.root_layout) {
            animateLayoutChanges = true
            layoutTransition = LayoutTransition().apply {
                setAnimateParentHierarchy(false)
            }
        }

        with(view.connections) {
            adapter = connectionsAdapter.apply {
                emptyView = view.empty_view

                onConnectionClick = {
                    connectionsViewModel.showConnectionInfo(bottomSheetManager, it)

                    // Running the outside code should be happened here, I think.
                    onShowConnectionOnMap(it)
                }

                onConnectionLongClick = {
                    connectionsViewModel.askDeleteConnection(activity, it)
                }

                onRefreshClick = {
                    connectionsViewModel.refreshConnection(it)
                }
            }
        }

        with(view.incoming_requests) {
            adapter = requestsAdapter.apply {
                companionView = view.incoming_requests_layout

                onRequestClick = {
                    connectionsViewModel.showRequestInfo(bottomSheetManager, it)
                }
                onRequestLongClick = {
                    connectionsViewModel.askDeleteRequest(activity, it)
                }
            }
        }
    }
}