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

import androidx.fragment.app.FragmentActivity
import com.potados.geomms.common.base.BaseViewModel
import com.potados.geomms.common.manager.BottomSheetManager
import com.potados.geomms.common.navigation.Navigator
import com.potados.geomms.model.Connection
import com.potados.geomms.model.ConnectionRequest
import com.potados.geomms.service.LocationSupportService
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

    fun refreshConnection(connection: Connection) {
        service.requestUpdate(connection.id)
    }
}