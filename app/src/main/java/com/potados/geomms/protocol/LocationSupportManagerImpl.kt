package com.potados.geomms.protocol

import android.content.Context
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.telephony.SmsManager
import com.potados.geomms.data.entity.LocationSupportConnection
import com.potados.geomms.data.entity.LocationSupportPacket
import com.potados.geomms.data.entity.LocationSupportPerson
import com.potados.geomms.util.DateTime
import com.potados.geomms.util.Metric
import com.potados.geomms.util.Notify

/**
 * LocationSupport 시스템입니다.
 *
 * 연결과 관련된 비즈니스 로직을 포함합니다.
 */
class LocationSupportManagerImpl(
    private val smsManager: SmsManager,
    private val locationManager: LocationManager,
    private val context: Context
) : LocationSupportManager, LocationListener {

    private var currentLocation: Location? = null

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


    private fun updateConnectionWithSentPacket(
        connection: LocationSupportConnection,
        packet: LocationSupportPacket
    ) {
        connection.setLastSentPacket(packet)
    }

    private fun updateConnectionWithReceivedPacket(
        connection: LocationSupportConnection,
        packet: LocationSupportPacket
    ) {
        connection.setLastReceivedPacket(packet)

        currentLocation?.let {
            connection.setLastSeenDistance(
                Metric.fromDistanceBetween(it, Location("").apply {
                    latitude = packet.latitude
                    longitude = packet.longitude
                })
            )
        }
    }

    companion object {

    }
}