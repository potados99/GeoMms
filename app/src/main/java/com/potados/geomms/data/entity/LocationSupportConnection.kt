package com.potados.geomms.data.entity

import android.location.Location
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.potados.geomms.util.DateTime
import com.potados.geomms.util.Metric
import java.util.*

/**
 * LocationSupportManagerImpl 시스템의 연결 정보를 표현합니다.
 */
data class LocationSupportConnection(
    val person: LocationSupportPerson,
    val establishedTime: Long
) {

    /**
     * 연결 id.
     */
    val connectionId: Int = generateConnectionId()

    /**
     * 마지막 발신 패킷.
     */
    private val lastSentPacket = MutableLiveData<LocationSupportPacket>()
    fun getLastSentPacket(): LiveData<LocationSupportPacket> = lastSentPacket
    fun setLastSentPacket(packet: LocationSupportPacket) {
        lastSentPacket.value = packet
        setLastSentTime(DateTime.now())
    }

    /**
     * 마지막 수신 패킷.
     */
    private val lastReceivedPacket = MutableLiveData<LocationSupportPacket>()
    fun getLastReceivedPacket(): LiveData<LocationSupportPacket> = lastReceivedPacket
    fun setLastReceivedPacket(packet: LocationSupportPacket) {
        lastReceivedPacket.value = packet
        setLastReceivedTime(DateTime.now())
    }

    /**
     * 마지막 발신 시간.
     * lastSentPacket에 의존적임.
     */
    private val lastSentTime = MutableLiveData<DateTime>()
    fun getLastSentTime(): LiveData<DateTime> = lastSentTime
    private fun setLastSentTime(time: DateTime) {
        lastSentTime.value = time
    }

    /**
     * 마지막 수신 시간.
     * lastReceivedPacket에 의존적임.
     */
    private val lastReceivedTime = MutableLiveData<DateTime>()
    fun getLastReceivedTime(): LiveData<DateTime> = lastReceivedTime
    private fun setLastReceivedTime(time: DateTime) {
        lastReceivedTime.value = time
    }

    /**
     * 상대방 마지막 거리.
     * 단위: m(미터)
     */
    private val lastSeenDistance = MutableLiveData<Metric>()
    fun getLastSeenDistance(): LiveData<Metric> = lastSeenDistance
    fun setLastSeenDistance(metric: Metric) {
        lastSeenDistance.value = metric
    }

    init {

    }

    companion object {
        private fun generateConnectionId(): Int = 1

    }
}