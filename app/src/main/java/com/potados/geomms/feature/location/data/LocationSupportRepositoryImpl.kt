package com.potados.geomms.feature.location.data

import com.potados.geomms.core.functional.Result
import com.potados.geomms.core.interactor.UseCase.None
import com.potados.geomms.feature.location.LocationSupportProtocol
import com.potados.geomms.feature.location.LocationSupportService
import kotlin.Exception

class LocationSupportRepositoryImpl(
    private val service: LocationSupportService
) : LocationSupportRepository {

    override fun handleMessage(address: String, body: String): Result<LocationSupportPacket> =
        try {
            val parsed = LocationSupportProtocol.parse(body)
                ?: throw IllegalArgumentException("Parse failed.")

            service.onPacketReceived(address, parsed)

            Result.Success(parsed)
        } catch (e: Exception) {
            Result.Error(e)
        }

    override fun requestNewConnection(request: LocationSupportRequest): Result<None> =
        try {
            service.requestNewConnection(request)

            Result.Success(None())
        } catch (e: Exception) {
            Result.Error(e)
        }

    override fun acceptRequest(request: LocationSupportRequest): Result<None> =
        try {
            service.acceptNewConnection(request)

            Result.Success(None())
        } catch (e: Exception) {
            Result.Error(e)
        }

    override fun requestLocation(connection: LocationSupportConnection): Result<None> =
        try {
            service.requestUpdate(connection)

            Result.Success(None())
        } catch (e: Exception) {
            Result.Error(e)
        }

    override fun sendLocation(connection: LocationSupportConnection): Result<None> =
        try {
            service.sendUpdate(connection)

            Result.Success(None())
        } catch (e: Exception) {
            Result.Error(e)
        }

    override fun getPendingRequests(): Result<List<LocationSupportRequest>> =
        try {
            val outgoing = service.getOutboundRequests()

            Result.Success(outgoing)
        } catch (e: Exception) {
            Result.Error(e)
        }

    override fun getIncomingRequests(): Result<List<LocationSupportRequest>> =
        try {
            val incoming = service.getInboundRequests()

            Result.Success(incoming)
        } catch (e: Exception) {
            Result.Error(e)
        }

    override fun getConnection(): Result<List<LocationSupportConnection>> =
        try {
            val connections = service.getConnections()

            Result.Success(connections)
        } catch (e: Exception) {
            Result.Error(e)
        }
}