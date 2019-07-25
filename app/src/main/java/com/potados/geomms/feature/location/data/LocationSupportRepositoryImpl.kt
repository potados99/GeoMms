package com.potados.geomms.feature.location.data

import com.potados.geomms.core.exception.Failure
import com.potados.geomms.core.functional.Either
import com.potados.geomms.core.interactor.UseCase
import com.potados.geomms.feature.location.LocationSupportFailure
import com.potados.geomms.feature.location.LocationSupportProtocol
import com.potados.geomms.feature.location.LocationSupportService
import kotlin.Exception

class LocationSupportRepositoryImpl(
    private val service: LocationSupportService
) : LocationSupportRepository {

    override fun handleMessage(address: String, body: String): Either<Failure, LocationSupportPacket> =
        try {
            val parsed = LocationSupportProtocol.parse(body)

            if (parsed == null) {
                Either.Left(LocationSupportFailure.PacketParseError())
            }
            else {
                service.onPacketReceived(address, parsed)

                Either.Right(parsed)
            }
        } catch (e: Exception) {
            when (e) {
                is IllegalArgumentException -> {
                    Either.Left(LocationSupportFailure.InvalidPacketError())
                }
                else -> {
                    Either.Left(LocationSupportFailure.HandleIncomingPacketFailure())
                }
            }
        }

    override fun requestNewConnection(request: LocationSupportRequest): Either<Failure, UseCase.None> =
        try {
            service.requestNewConnection(request)

            Either.Right(UseCase.None())
        } catch (e: Exception) {
            when (e) {
                is IllegalArgumentException -> {
                    Either.Left(LocationSupportFailure.InvalidRequestError())
                }
                else -> {
                    Either.Left(LocationSupportFailure.RequestFailure())
                }
            }
        }

    override fun acceptRequest(request: LocationSupportRequest): Either<Failure, UseCase.None> =
        try {
            service.acceptNewConnection(request)

            Either.Right(UseCase.None())
        } catch (e: Exception) {
            when (e) {
                is IllegalArgumentException -> {
                    Either.Left(LocationSupportFailure.InvalidRequestError())
                }
                else -> {
                    Either.Left(LocationSupportFailure.SendPacketFailure())
                }
            }
        }

    override fun requestLocation(connection: LocationSupportConnection): Either<Failure, UseCase.None> =
        try {
            service.requestUpdate(connection)

            Either.Right(UseCase.None())
        } catch (e: Exception) {
            when (e) {
                is IllegalArgumentException -> {
                    Either.Left(LocationSupportFailure.InvalidConnectionError())
                }
                else -> {
                    Either.Left(LocationSupportFailure.SendPacketFailure())
                }
            }
        }

    override fun sendLocation(connection: LocationSupportConnection): Either<Failure, UseCase.None> =
        try {
            service.sendUpdate(connection)

            Either.Right(UseCase.None())
        } catch (e: Exception) {
            when (e) {
                is IllegalArgumentException -> {
                    Either.Left(LocationSupportFailure.InvalidConnectionError())
                }
                else -> {
                    Either.Left(LocationSupportFailure.SendPacketFailure())
                }
            }
        }

    override fun getPendingRequests(): Either<Failure, List<LocationSupportRequest>> =
        try {
            val outgoing = service.getOutboundRequests()

            Either.Right(outgoing)
        } catch (e: Exception) {
            Either.Left(LocationSupportFailure.LocalDataFailure())
        }

    override fun getIncomingRequests(): Either<Failure, List<LocationSupportRequest>> =
        try {
            val incoming = service.getInboundRequests()

            Either.Right(incoming)
        } catch (e: Exception) {
            Either.Left(LocationSupportFailure.LocalDataFailure())
        }

    override fun getConnection(): Either<Failure, List<LocationSupportConnection>> =
        try {
            val connections = service.getConnections()

            Either.Right(connections)
        } catch (e: Exception) {
            Either.Left(LocationSupportFailure.LocalDataFailure())
        }
}