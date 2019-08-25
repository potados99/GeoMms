package com.potados.geomms.feature.location

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.potados.geomms.extension.tryOrNull
import com.potados.geomms.model.Connection
import com.potados.geomms.model.ConnectionRequest
import com.potados.geomms.service.LocationSupportService
import io.realm.RealmResults
import org.koin.core.KoinComponent
import org.koin.core.inject
import timber.log.Timber
import java.util.*

/**
 * MapFragment를 보조할 뷰모델입니다.
 * 지도에 꽂히는 핀 정보, 친구 목록, 연결 정보 등을 가집니다.
 */
class MapViewModel : ViewModel(), KoinComponent {

    private val locationService: LocationSupportService by inject()

    val incomingRequests = locationService.getIncomingRequests()
    val outgoingRequests = locationService.getOutgoingRequests()
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

                _markers.add(markerOption)

                Timber.i("Marker added.")
            }
        }

        // commit
        _liveMarkers.value = _markers
    }

    fun start() {
        // Setting chagne listener does not invoke it on register time.
        // Do it for one time manually.
        connections.addChangeListener(refreshMarkers)
        refreshMarkers(connections)
    }

    fun request(address: String) {
        locationService.requestNewConnection(address, 1800000)
    }

    fun accept(request: ConnectionRequest) {
        tryOrNull {
            locationService.acceptConnectionRequest(request)
        }
    }

    fun refuse(request: ConnectionRequest) {
        tryOrNull {
            locationService.refuseConnectionRequest(request)
        }
    }

    fun delete(connection: Connection) {
        tryOrNull {
            locationService.requestDisconnect(connection.id)
        }
    }
}