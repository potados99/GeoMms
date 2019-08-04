package com.potados.geomms.feature.location.data

import com.potados.geomms.core.functional.Result
import com.potados.geomms.core.interactor.UseCase.None

interface LocationSupportRepository {

    /**
     * 패킷이 도착했을 때에 실행.
     */
    fun handleMessage(address: String, body: String): Result<LocationSupportPacket>

    /**
     * 새로운 연결을 요청.
     */
    fun requestNewConnection(request: LocationSupportRequest): Result<None>

    /**
     * 연결 요청을 수락.
     */
    fun acceptRequest(request: LocationSupportRequest): Result<None>

    /**
     * 상대 위치 즉각 요청.
     */
    fun requestLocation(connection: LocationSupportConnection): Result<None>

    /**
     * 현재 위치 전송.
     */
    fun sendLocation(connection: LocationSupportConnection): Result<None>

    /**
     * 상대방의 수락을 대기중인 요청.
     */
    fun getPendingRequests(): Result<List<LocationSupportRequest>>

    /**
     * 나의 수락을 기다리는 요청.
     */
    fun getIncomingRequests(): Result<List<LocationSupportRequest>>

    /**
     * 현재 진행중인 연결
     */
    fun getConnection(): Result<List<LocationSupportConnection>>
}