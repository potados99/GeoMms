package com.potados.geomms.feature.location.presentation

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.android.gms.maps.model.Marker
import com.potados.geomms.feature.location.data.LSConnection
import com.potados.geomms.feature.location.data.LSRequest
import com.potados.geomms.feature.location.domain.LSProtocol
import com.potados.geomms.feature.location.domain.LSService
import org.koin.core.KoinComponent
import org.koin.core.inject

/**
 * MapFragment를 보조할 뷰모델입니다.
 * 지도에 꽂히는 핀 정보, 친구 목록, 연결 정보 등을 가집니다.
 */
class MapViewModel : ViewModel(), KoinComponent {

    /***********************************************************
     * UseCase
     ***********************************************************/

    private val lsService: LSService by inject()

    val connections = MutableLiveData<List<LSConnection>>()
    val incomingRequests = MutableLiveData<List<LSRequest>>()
    val outgoingRequests = MutableLiveData<List<LSRequest>>()

    val markers = mutableListOf<Marker>()

    fun requestNewConnection(address: String, lifeSpan: Long) =
        lsService.requestNewConnection(
            LSRequest
                .builder()
                .setAsOutBound()
                .setPerson(address)
                .setLifeSpan(lifeSpan)
                .build()
        )

    fun acceptRequest(request: LSRequest) =
        lsService.acceptNewConnection(request)

    fun requestLocation(connection: LSConnection) =
        lsService.requestUpdate(connection)

    fun sendLocation(connection: LSConnection) =
        lsService.sendUpdate(connection)

}