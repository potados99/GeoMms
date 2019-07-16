package com.potados.geomms.protocol

import android.telephony.SmsManager
import com.potados.geomms.data.entity.LocationSupportConnection
import com.potados.geomms.data.entity.LocationSupportPacket
import com.potados.geomms.data.entity.LocationSupportPerson
import com.potados.geomms.util.DateTime

/**
 * LocationSupportManagerImpl 시스템입니다.
 *
 * 연결과 관련된 비즈니스 로직을 포함합니다.
 */
class LocationSupportManagerImpl(private val smsManager: SmsManager) : LocationSupportManager {

    private val connections = mutableListOf<LocationSupportConnection>()

    override fun onPacketReceived(packet: LocationSupportPacket) {

    }

    override fun requestNewConnection(person: LocationSupportPerson) {
        connections.add(
            LocationSupportConnection(person, DateTime.getCurrentTimeStamp())
        )
    }

    override fun acceptNewConnection(person: LocationSupportPerson, reqPacket: LocationSupportPacket) {

    }

    override fun deleteConnection(connection: LocationSupportConnection) {

    }

    override fun getConnections(): List<LocationSupportConnection> {
        return connections
    }

    override fun requestUpdate(connection: LocationSupportConnection) {

    }

    override fun sendUpdate(connection: LocationSupportConnection) {

    }

    private fun sendData(person: LocationSupportPerson) {

    }

    private fun sendRequest(person: LocationSupportPerson) {

    }

    companion object {

    }
}