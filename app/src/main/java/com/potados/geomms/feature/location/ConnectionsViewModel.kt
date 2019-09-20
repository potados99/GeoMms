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

import androidx.fragment.app.FragmentActivity
import com.google.android.gms.maps.GoogleMap
import com.potados.geomms.R
import com.potados.geomms.common.base.BaseViewModel
import com.potados.geomms.common.extension.moveTo
import com.potados.geomms.common.manager.BottomSheetManager
import com.potados.geomms.common.navigation.Navigator
import com.potados.geomms.extension.withNonNull
import com.potados.geomms.model.Connection
import com.potados.geomms.model.ConnectionRequest
import com.potados.geomms.service.LocationSupportService
import com.potados.geomms.util.Notify
import com.potados.geomms.util.Popup
import org.koin.core.inject

class ConnectionsViewModel : BaseViewModel() {

    private val service: LocationSupportService by inject()
    private val locationService: LocationSupportService by inject()
    private val navigator: Navigator by inject()

    val incomingRequests = locationService.getIncomingRequests()
    val connections = locationService.getConnections()

    init {
        failables += locationService
        failables += navigator
    }

    fun invite() {
        navigator.showInvite()
    }

    fun showConnectionInfo(sheetManager: BottomSheetManager?, connection: Connection) {
        sheetManager?.let {
            navigator.showConnectionInfo(it, connection.id)
        }
    }

    fun showRequestInfo(sheetManager: BottomSheetManager?, request: ConnectionRequest) {
        sheetManager?.let {
            navigator.showRequestInfo(it, request.connectionId)
        }
    }

    fun askDeleteConnection(activity: FragmentActivity?, connection: Connection) {
        // TODO
        // Delete connection here directly.
    }

    fun askDeleteRequest(activity: FragmentActivity?, request: ConnectionRequest) {
        // TODO
        // Delete request here directly.
    }

    fun refreshConnection(connection: Connection) {
        service.requestUpdate(connection.id)
    }
}