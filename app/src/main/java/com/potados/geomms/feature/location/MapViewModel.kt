package com.potados.geomms.feature.location

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.android.gms.maps.model.Marker
import com.potados.geomms.model.Connection
import com.potados.geomms.service.LocationSupportService
import io.realm.RealmResults
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
    // ...

    private val locationService: LocationSupportService by inject()

    val incomingRequests = locationService.getIncomingRequests()
    val outgoingRequests = locationService.getOutgoingRequests()
    val connections = locationService.getConnections()
}