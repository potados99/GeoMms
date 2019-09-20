package com.potados.geomms.feature.location

import androidx.fragment.app.FragmentActivity
import com.google.android.gms.maps.GoogleMap
import com.potados.geomms.R
import com.potados.geomms.common.base.BaseViewModel
import com.potados.geomms.common.extension.moveTo
import com.potados.geomms.common.navigation.Navigator
import com.potados.geomms.extension.withNonNull
import com.potados.geomms.model.Connection
import com.potados.geomms.model.ConnectionRequest
import com.potados.geomms.service.LocationSupportService
import com.potados.geomms.util.Notify
import com.potados.geomms.util.Popup
import org.koin.core.inject

class ConnectionsViewModel : BaseViewModel() {

    private val locationService: LocationSupportService by inject()
    private val navigator: Navigator by inject()

    val incomingRequests = locationService.getIncomingRequests()
    val connections = locationService.getConnections()

    fun invite() {
        navigator.showInvite()
    }

    fun showConnectionInfo(connection: Connection) {
        navigator.showConnectionInfo(connection.id)


    }

    fun showRequestInfo(request: ConnectionRequest) {
        navigator.showRequestInfo(request.connectionId)
    }

    fun askDeleteConnection(activity: FragmentActivity?, connection: Connection) {
        // TODO
        // Delete connection here directly.
    }

    fun askDeleteRequest(activity: FragmentActivity?, request: ConnectionRequest) {
        // TODO
        // Delete request here directly.
    }
}