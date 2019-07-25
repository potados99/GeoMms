package com.potados.geomms.feature.location.data

import com.potados.geomms.core.exception.Failure
import com.potados.geomms.core.functional.Either
import com.potados.geomms.core.interactor.UseCase

interface LocationSupportRepository {

    /**
     * 패킷이 도착했을 때에 실행.
     */
    fun handleMessage(address: String, body: String): Either<Failure, LocationSupportPacket>

    /**
     * 새로운 연결을 요청.
     */
    fun requestNewConnection(request: LocationSupportRequest): Either<Failure, UseCase.None>

    /**
     * 연결 요청을 수락.
     */
    fun acceptRequest(request: LocationSupportRequest): Either<Failure, UseCase.None>

    /**
     * 상대 위치 즉각 요청.
     */
    fun requestLocation(connection: LocationSupportConnection): Either<Failure, UseCase.None>

    /**
     * 현재 위치 전송.
     */
    fun sendLocation(connection: LocationSupportConnection): Either<Failure, UseCase.None>

    /**
     * 상대방의 수락을 대기중인 요청.
     */
    fun getPendingRequests(): Either<Failure, List<LocationSupportRequest>>

    /**
     * 나의 수락을 기다리는 요청.
     */
    fun getIncomingRequests(): Either<Failure, List<LocationSupportRequest>>

    /**
     * 현재 진행중인 연결
     */
    fun getConnection(): Either<Failure, List<LocationSupportConnection>>
}