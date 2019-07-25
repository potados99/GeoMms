package com.potados.geomms.feature.location

import com.potados.geomms.core.exception.Failure

class LocationSupportFailure {
    class HandleIncomingPacketFailure : Failure.FeatureFailure()
    class PacketParseError: Failure.FeatureFailure()
    class InvalidPacketError : Failure.FeatureFailure()
    class InvalidRequestError : Failure.FeatureFailure()
    class InvalidConnectionError: Failure.FeatureFailure()
    class RequestFailure : Failure.FeatureFailure()
    class SendPacketFailure : Failure.FeatureFailure()
    class LocalDataFailure: Failure.FeatureFailure()
}