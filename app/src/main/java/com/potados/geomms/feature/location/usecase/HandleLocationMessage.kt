package com.potados.geomms.feature.location.usecase

import com.potados.geomms.core.functional.Result
import com.potados.geomms.core.interactor.UseCase
import com.potados.geomms.feature.location.data.LocationSupportPacket
import com.potados.geomms.feature.location.data.LocationSupportRepository

class HandleLocationMessage(
    private val lsRepository: LocationSupportRepository
) : UseCase<LocationSupportPacket, Pair<String, String>>() {

    override suspend fun run(params: Pair<String, String>): Result<LocationSupportPacket> =
        lsRepository.handleMessage(params.first, params.second)
}