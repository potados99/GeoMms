package com.potados.geomms.feature.data.repository

interface LocationSupportRepository {

    /**
     * 패킷이 도착했을 때에 실행됨.
     */
    fun onPacketReceieved()

    /**
     * 새로운 연결을 요청
     */
    fun requestNewConnection()

    /**
     * 상대방의 수락을 대기중인 요청
     */
    fun getPendingRequests()

    /**
     * 나의 수락을 기다리는 요청
     */
    fun getIncommingRequests()

}