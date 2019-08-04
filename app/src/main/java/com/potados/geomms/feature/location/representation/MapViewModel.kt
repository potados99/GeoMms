package com.potados.geomms.feature.location.representation

import androidx.lifecycle.MutableLiveData
import com.google.android.gms.maps.model.Marker
import com.potados.geomms.core.interactor.UseCase.None
import com.potados.geomms.core.platform.BaseViewModel
import com.potados.geomms.feature.common.ContactRepository
import com.potados.geomms.feature.location.data.LSConnection
import com.potados.geomms.feature.location.data.LSRequest
import com.potados.geomms.feature.location.domain.LSProtocol
import com.potados.geomms.feature.location.domain.usecase.*
import org.koin.core.KoinComponent
import org.koin.core.inject

/**
 * MapFragment를 보조할 뷰모델입니다.
 * 지도에 꽂히는 핀 정보, 친구 목록, 연결 정보 등을 가집니다.
 */
class MapViewModel : BaseViewModel(), KoinComponent {

    /***********************************************************
     * UseCase
     ***********************************************************/
    private val handlePacket: HandleLocationMessage by inject()
    private val requestConnection: RequestConnection by inject()
    private val acceptRequest: AcceptNewConnection by inject()
    private val requestLocation: RequestLocation by inject()
    private val sendLocation: SendLocation by inject()
    private val getReqOut: GetOutboundRequests by inject()
    private val getReqIn: GetInboundRequests by inject()
    private val getConnections: GetConnections by inject()

    private val contactRepository: ContactRepository by inject()

    val connections = MutableLiveData<List<LSConnection>>()
    val incomingRequests = MutableLiveData<List<LSRequest>>()
    val outgoingRequests = MutableLiveData<List<LSRequest>>()

    val markers = mutableListOf<Marker>()

    fun onMessageReceived(address: String, body: String) =
        if (LSProtocol.isLocationSupportMessage(body))
            handlePacket(Pair(address, body)) { it.onError(::handleFailure) }
        else {
            /**
             * 무시합니다~
             */
        }

    fun requestNewConnection(address: String, lifeSpan: Long) =
        requestConnection(
            LSRequest
                .builder()
                .setAsOutBound()
                .setPerson(address)
                .setLifeSpan(lifeSpan)
                .build()
        ) {
            it.onError(::handleFailure)
        }

    fun acceptRequest(request: LSRequest) =
        acceptRequest(request) { it.either({ loadAll() }, ::handleFailure) }

    fun requestLocation(connection: LSConnection) =
        requestLocation(connection) {
            it.onError(::handleFailure)
        }

    fun sendLocation(connection: LSConnection) =
        sendLocation(connection) {
            it.onError(::handleFailure)
        }

    fun loadConnections() =
        getConnections(None()) { it.either( { connections.value = it }, ::handleFailure) }

    fun loadIncommingRequests() =
        getReqIn(None()) { it.either({ incomingRequests.value = it }, ::handleFailure) }

    fun loadOutgoingRequests() =
        getReqOut(None()) { it.either({ outgoingRequests.value = it }, ::handleFailure) }

    fun loadAll() {
        loadConnections()
        loadIncommingRequests()
        loadOutgoingRequests()
    }

    fun getName(address: String): String {
        return contactRepository.getContactNameByPhoneNumber(address) ?: address
    }
}