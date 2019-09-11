package com.potados.geomms.feature.location

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.location.Location
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.potados.geomms.R
import com.potados.geomms.common.base.BaseViewModel
import com.potados.geomms.common.extension.baseActivity
import com.potados.geomms.common.extension.moveTo
import com.potados.geomms.common.navigation.Navigator
import com.potados.geomms.common.util.DateFormatter
import com.potados.geomms.common.widget.AvatarView
import com.potados.geomms.extension.tryOrNull
import com.potados.geomms.extension.withNonNull
import com.potados.geomms.model.Connection
import com.potados.geomms.model.ConnectionRequest
import com.potados.geomms.repository.LocationRepository
import com.potados.geomms.service.LocationSupportService
import com.potados.geomms.util.Notify
import com.potados.geomms.util.Popup
import io.realm.RealmResults
import kotlinx.android.synthetic.main.marker_layout.view.*
import org.koin.android.ext.android.inject
import org.koin.core.KoinComponent
import org.koin.core.inject
import timber.log.Timber
import java.util.*

/**
 * View Model for [MapFragment].
 *
 * @see [MapFragment]
 */
class MapViewModel : BaseViewModel(), KoinComponent {

    private val locationService: LocationSupportService by inject()
    private val locationRepo: LocationRepository by inject()
    private val dateFormatter: DateFormatter by inject()
    private val navigator: Navigator by inject()

    val incomingRequests = locationService.getIncomingRequests()
    val connections = locationService.getConnections()

    private val _markers = mutableListOf<MarkerOptions>()
    val markers =  MutableLiveData<MutableList<MarkerOptions>>()

    private val refreshMarkers = { connections: RealmResults<Connection> ->
        _markers.clear()
        connections.forEach {
            if (it.lastUpdate != 0L) {
                // do not display marker when having nothing to display.

                val update = dateFormatter.getConversationTimestamp(it.lastUpdate)
                val lastUpdateString = context.getString(R.string.connection_last_update, update)

                val markerOption = MarkerOptions()
                    .position(LatLng(it.latitude, it.longitude))
                    .title(it.recipient?.getDisplayName())
                    .snippet(lastUpdateString)
                    .icon(BitmapDescriptorFactory.defaultMarker(Random(it.id).nextInt(360).toFloat()))

                _markers.add(markerOption)

                Timber.i("Marker added.")
            }
        }

        // commit
        markers.value = _markers
    }

    init {
        failables += this
        failables += locationService
        failables += locationRepo
        failables += dateFormatter
        failables += navigator
    }

    override fun start() {
        super.start()

        connections?.let {
            // Setting chagne listener does not invoke it on register time.
            // Do it for one time manually.
            it.addChangeListener(refreshMarkers)
            refreshMarkers(it)
        }
    }

    private fun request(address: String) = locationService.requestNewConnection(address, 1800000)
    private fun accept(request: ConnectionRequest) = locationService.acceptConnectionRequest(request)
    private fun refuse(request: ConnectionRequest) = locationService.refuseConnectionRequest(request)
    private fun delete(connection: Connection) = locationService.requestDisconnect(connection.id)
    private fun cancel(connection: Connection) = locationService.cancelConnectionRequest(connection)

    fun invite() {
        navigator.showInvite()
        Timber.i("Invite clicked!")
    }

    fun request(activity: FragmentActivity?, address: String) {
        request(address)
        Popup(activity)
            .withTitle(R.string.title_invitation_sent)
            .withMessage(R.string.connection_request_sent_to, address)
            .withPositiveButton(R.string.button_ok)
            .show()
    }

    fun showConnectionInfo(activity: FragmentActivity?, map: GoogleMap?, connection: Connection) {
        if (connection.isTemporal) {
            // Sent request not accepted yet
            Notify(activity).short(R.string.notify_request_not_yet_accepted)
            return
        }

        withNonNull(map) {
            if (connection.lastUpdate != 0L) {
                moveTo(connection.latitude, connection.longitude, 15f)
            } else {
                Notify(activity).short(R.string.notify_no_location_data)
            }
        }
    }

    fun showConnectionDeletionConfirmation(activity: FragmentActivity?, connection: Connection) {
        if (connection.isTemporal) {
            Popup(activity)
                .withTitle(R.string.title_cancel_request)
                .withMessage(R.string.dialog_ask_cancel_request, connection.recipient?.getDisplayName())
                .withPositiveButton(R.string.button_yes) { cancel(connection) }
                .withNegativeButton(R.string.button_no)
                .show()

        } else {
            Popup(activity)
                .withTitle(R.string.title_disconnect)
                .withMessage(R.string.dialog_ask_disconnect, connection.recipient?.getDisplayName())
                .withPositiveButton(R.string.button_confirm) { delete(connection) }
                .withNegativeButton(R.string.button_cancel)
                .show()
        }
    }

    fun showRequestInfo(activity: FragmentActivity?, request: ConnectionRequest) {
        Popup(activity)
            .withTitle(R.string.dialog_request_from, request.recipient?.getDisplayName())
            .withMessage(R.string.dialog_ask_accept_request, request.recipient?.getDisplayName(), dateFormatter.getDuration(request.duration))
            .withPositiveButton(R.string.button_accept) { accept(request) }
            .withNegativeButton(R.string.button_later)
            .show()
    }

    fun showRequestDeletionConfirmation(activity: FragmentActivity?, request: ConnectionRequest) {
        Popup(activity)
            .withTitle(R.string.dialog_refuse_request)
            .withMessage(R.string.dialog_ask_refuse, request.recipient?.getDisplayName())
            .withPositiveButton(R.string.button_refuse) { refuse(request)  }
            .withNegativeButton(R.string.button_cancel)
            .show()
    }

    fun getLocation(onLocation: (Location) -> Unit) {
        locationRepo.getLocationWithCallback(onLocation)
    }
}