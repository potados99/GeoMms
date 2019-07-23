package com.potados.geomms.feature.protocol

import android.content.Context
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.telephony.SmsManager
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.potados.geomms.feature.data.entity.LocationSupportConnection
import com.potados.geomms.feature.data.entity.LocationSupportPacket
import com.potados.geomms.feature.data.entity.LocationSupportPerson
import com.potados.geomms.core.util.DateTime
import com.potados.geomms.core.util.Metric
import com.potados.geomms.core.util.Notify
import com.potados.geomms.feature.protocol.LocationSupportProtocol.Companion.findType

/**
 * LocationSupport 시스템입니다.
 *
 * 연결과 관련된 비즈니스 로직을 포함합니다.
 */
class LocationSupportManagerImpl(
    private val context: Context,
    private val smsManager: SmsManager,
    private val locationManager: LocationManager
) : LocationSupportManager, LocationListener {

    private var currentLocation: Location? = null

    private val connections = mutableListOf<LocationSupportConnection>()
    private val liveConnections = MutableLiveData<List<LocationSupportConnection>>()

    private val connectionsWaitingForAccept = mutableListOf<LocationSupportConnection>()

    init {
        try {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5000, 5.0f, this)
        }
        catch (e: SecurityException) {
            Notify.short(context, "Cannot get location.")
        }
    }

    override fun onLocationChanged(location: Location?) {
        currentLocation = location
    }

    override fun onProviderDisabled(provider: String?) {
    }

    override fun onProviderEnabled(provider: String?) {
    }

    override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {
    }

    override fun onPacketReceived(packet: LocationSupportPacket, address: String) {

        when (findType(packet.type)) {
            LocationSupportProtocol.Companion.PacketType.ACCEPT_CONNECT -> {
                Log.d("LocationSupportManagerImpl:onPacketReceived", "ACCEPT_CONNECT packet.")
                val waiting = connectionsWaitingForAccept.find {
                    it.person.address == address
                }

                waiting?.let {
                    it.establishedTime = DateTime.getCurrentTimeStamp()
                    connections.add(it)
                    liveConnections.value = connections

                    Log.d("LocationSupportManagerImpl:onPacketReceived", "connection established.")
                }
            }

            LocationSupportProtocol.Companion.PacketType.DATA -> {
                val connection = connections.find {
                    it.person.address == address
                }

                connection?.let {
                    updateConnectionWithReceivedPacket(connection, packet)
                }
            }

            else -> {
                /**/
            }
        }
    }

    override fun requestNewConnection(person: LocationSupportPerson) {
        // TODO: id 바꾸기
        val packet = LocationSupportProtocol.createRequestConnectPacket(
            4096, 1800000 /* 30분 */
        )

        val payload = LocationSupportProtocol.serialize(packet) ?: return

        smsManager.sendTextMessage(
            person.address,
            null,
            payload,
            null,
            null)

        connectionsWaitingForAccept.add(
            LocationSupportConnection(person, 0)
        )

        Log.d("GEOMMS", payload)
    }

    override fun acceptNewConnection(person: LocationSupportPerson, reqPacket: LocationSupportPacket) {

    }

    override fun deleteConnection(connection: LocationSupportConnection) {

    }

    override fun getConnections(): LiveData<List<LocationSupportConnection>> {
        return liveConnections
    }

    override fun requestUpdate(connection: LocationSupportConnection) {

    }

    override fun sendUpdate(connection: LocationSupportConnection) {

    }

    private fun sendData(person: LocationSupportPerson) {

    }

    private fun sendRequest(person: LocationSupportPerson) {

    }


    private fun updateConnectionWithSentPacket(
        connection: LocationSupportConnection,
        packet: LocationSupportPacket
    ) {
        connection.lastSentPacket = packet

        liveConnections.value = connections
    }

    private fun updateConnectionWithReceivedPacket(
        connection: LocationSupportConnection,
        packet: LocationSupportPacket
    ) {
        connection.lastReceivedPacket = packet
        connection.lastReceivedTime = DateTime.now()

        currentLocation?.let {
            connection.lastSeenDistance = Metric.fromDistanceBetween(
                it,
                Location("").apply {
                    latitude = packet.latitude
                    longitude = packet.longitude
                }
            )
        }

        liveConnections.value = connections
    }

    override fun onEverySecondUpdate() {
        liveConnections.value = connections
    }

    companion object {

    }
}