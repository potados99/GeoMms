package com.potados.geomms.feature.location

import android.location.Location
import androidx.lifecycle.MutableLiveData
import com.potados.geomms.core.interactor.UseCase
import com.potados.geomms.core.platform.BaseViewModel
import com.potados.geomms.feature.common.ContactRepository
import com.potados.geomms.feature.location.LocationSupportProtocol.Companion.findType
import com.potados.geomms.feature.location.data.LocationSupportConnection
import com.potados.geomms.feature.location.data.LocationSupportPacket
import com.potados.geomms.feature.location.data.LocationSupportRequest
import com.potados.geomms.feature.location.usecase.*
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
    private val acceptRequest: AcceptRequest by inject()
    private val requestLocation: RequestLocation by inject()
    private val sendLocation: SendLocation by inject()
    private val getReqOut: GetPendingRequests by inject()
    private val getReqIn: GetIncommingRequests by inject()
    private val getConnections: GetConnections by inject()

    private val contactRepo: ContactRepository by inject()

    val connections = MutableLiveData<List<LocationSupportConnection>>()
    val incomingRequests = MutableLiveData<List<LocationSupportRequest>>()

    fun onMessageReceived(address: String, body: String) =
        handlePacket(Pair(address, body)) {
            it.either(::handleFailure) { validPacket ->

                /**
                 * 이 루틴은 패킷이 적절하게 처리되었을 때에만 수행됩니다.
                 * 패킷의 유형에 따라 변경된 데이터를 선택적으로 새로 불러옵니다.
                 *
                 * 예를 들어 새 연결을 요청하는 패킷은 사용자에게 보여져야 하므로 UI에 영향을 미칩니다.
                 * 따라서 새로 들어오는 요청이 있는지 [loadIncommingRequests]를 통해 확인합니다.
                 */
                when (findType(validPacket.type)) {
                    LocationSupportPacket.Companion.PacketType.REQUEST_CONNECT ->
                        loadIncommingRequests()

                    LocationSupportPacket.Companion.PacketType.ACCEPT_CONNECT ->
                        loadConnections()

                    LocationSupportPacket.Companion.PacketType.DATA ->
                        loadConnections()

                    else -> {
                        /** 나머지는 UI에 영향 없는 패킷들입니다. */
                    }
                }
            }
        }

    fun requestNewConnection(address: String, lifeSpan: Long) =
        requestConnection(
            LocationSupportRequest
                .builder()
                .setAsOutBound()
                .setPerson(address)
                .setLifeSpan(lifeSpan)
                .build()
        ) {
            it.either(::handleFailure) { }
        }

    fun acceptRequest(request: LocationSupportRequest) =
        acceptRequest(request) {
            it.either(::handleFailure) {
                /**
                 * 연결을 수락했으면 새로운 연결이 생겼을 것이므로 다시 불러옵니다.
                 */
                loadConnections()
            }
        }

    fun requestLocation(connection: LocationSupportConnection) =
        requestLocation(connection) {
            it.either(::handleFailure) {
                /** 아무것도 안해도 돼요~ */
            }
        }

    fun sendLocation(connection: LocationSupportConnection) =
        sendLocation(connection) {
            it.either(::handleFailure) {
                /** UI는 할게 없어요~ */
            }
        }

    fun loadConnections() =
        getConnections(UseCase.None()) {
            it.either(::handleFailure) { right ->
                connections.value = right
                1
            }
        }

    fun loadIncommingRequests() =
        getReqIn(UseCase.None()) {
            it.either(::handleFailure) { right ->
                incomingRequests.apply { value = right }
            }
        }


    fun getName(address: String) = contactRepo.getContactNameByPhoneNumber(address)
}