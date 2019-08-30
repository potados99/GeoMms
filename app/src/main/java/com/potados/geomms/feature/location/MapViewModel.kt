package com.potados.geomms.feature.location

import android.content.Context
import android.view.LayoutInflater
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.potados.geomms.R
import com.potados.geomms.common.base.BaseViewModel
import com.potados.geomms.extension.tryOrNull
import com.potados.geomms.model.Connection
import com.potados.geomms.model.ConnectionRequest
import com.potados.geomms.service.LocationSupportService
import io.realm.RealmResults
import kotlinx.android.synthetic.main.marker_layout.view.*
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
    private val context: Context by inject()

    val incomingRequests = locationService.getIncomingRequests()
    val connections = locationService.getConnections()

    private val _markers = mutableListOf<MarkerOptions>()
    private val _liveMarkers = MutableLiveData<MutableList<MarkerOptions>>()
    val markers: LiveData<MutableList<MarkerOptions>> = _liveMarkers

    private val refreshMarkers = { connections: RealmResults<Connection> ->
        _markers.clear()
        connections.forEach {
            if (it.lastUpdate != 0L) {
                // do not display marker when having nothing to display.

                val markerOption = MarkerOptions()
                    .position(LatLng(it.latitude, it.longitude))
                    .title(it.recipient?.getDisplayName())
                    .icon(BitmapDescriptorFactory.defaultMarker(Random(it.id).nextInt(360).toFloat()))

                val markerAvatar = LayoutInflater
                    .from(context)
                    .inflate(R.layout.marker_layout, null)
                    .avatar

                markerAvatar.setContact(it.recipient)

                _markers.add(markerOption)

                Timber.i("Marker added.")
            }
        }

        // commit
        _liveMarkers.value = _markers
    }

    init {
        failables += this
        failables += locationService
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

    /**
     * Add a non-accepted outgoing request as a disabled connection
     * to a connections list.
     */
    fun request(address: String): Boolean {
        return tryOrNull {
            locationService.requestNewConnection(address, 1800000)
            return@tryOrNull true
        } ?: false
    }

    /**
     * Accept connection request.
     */
    fun accept(request: ConnectionRequest): Boolean {
        return tryOrNull {
            locationService.acceptConnectionRequest(request)
            return@tryOrNull true
        } ?: false
    }

    /**
     * Refuse connection request.
     */
    fun refuse(request: ConnectionRequest): Boolean {
        return tryOrNull {
            locationService.refuseConnectionRequest(request)
            return@tryOrNull true
        } ?: false
    }

    /**
     * Disconnect
     */
    fun delete(connection: Connection): Boolean {
        return tryOrNull {
            locationService.requestDisconnect(connection.id)
            return@tryOrNull true
        } ?: false
    }

    /**
     * Cancel request
     */
    fun cancel(connection: Connection): Boolean {
        return tryOrNull {
            locationService.cancelConnectionRequest(connection)
            return@tryOrNull true
        } ?: false
    }
}