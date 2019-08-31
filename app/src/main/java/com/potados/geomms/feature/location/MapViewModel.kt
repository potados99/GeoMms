package com.potados.geomms.feature.location

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.potados.geomms.R
import com.potados.geomms.common.base.BaseViewModel
import com.potados.geomms.common.util.DateFormatter
import com.potados.geomms.common.widget.AvatarView
import com.potados.geomms.extension.tryOrNull
import com.potados.geomms.model.Connection
import com.potados.geomms.model.ConnectionRequest
import com.potados.geomms.service.LocationSupportService
import com.potados.geomms.util.Popup
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
    private val dateFormatter: DateFormatter by inject()

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
    fun request(address: String) =
        locationService.requestNewConnection(address, 1800000)

    /**
     * Accept connection request.
     */
    fun accept(request: ConnectionRequest) =
        locationService.acceptConnectionRequest(request)

    /**
     * Refuse connection request.
     */
    fun refuse(request: ConnectionRequest) =
        locationService.refuseConnectionRequest(request)

    /**
     * Disconnect
     */
    fun delete(connection: Connection) =
        locationService.requestDisconnect(connection.id)

    /**
     * Cancel request
     */
    fun cancel(connection: Connection) =
        locationService.cancelConnectionRequest(connection)

    private fun getBitmapFromView(view: View): Bitmap {
        view.measure(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)

        val bitmap = Bitmap.createBitmap(
            view.measuredWidth,
            view.measuredHeight,
            Bitmap.Config.ARGB_8888
        )

        val canvas = Canvas(bitmap)

        view.layout(0, 0, view.measuredWidth, view.measuredHeight)

        view.draw(canvas)

        // Background included here.
        return bitmap
    }
}