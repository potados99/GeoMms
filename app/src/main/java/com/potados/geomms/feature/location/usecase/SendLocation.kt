package com.potados.geomms.feature.location.usecase

import com.potados.geomms.core.functional.Result
import com.potados.geomms.core.interactor.UseCase
import com.potados.geomms.feature.location.data.LocationSupportConnection
import com.potados.geomms.feature.location.data.LocationSupportRepository

class SendLocation(
    private val lsRepository: LocationSupportRepository
) : UseCase<UseCase.None, LocationSupportConnection>() {

    override suspend fun run(params: LocationSupportConnection): Result<None> =
        lsRepository.sendLocation(params)
}