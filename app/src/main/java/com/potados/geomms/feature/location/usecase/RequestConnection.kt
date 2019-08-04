package com.potados.geomms.feature.location.usecase

import com.potados.geomms.core.functional.Result
import com.potados.geomms.core.interactor.UseCase
import com.potados.geomms.feature.location.data.LocationSupportRepository
import com.potados.geomms.feature.location.data.LocationSupportRequest

class RequestConnection(
    private val lsRepository: LocationSupportRepository
) : UseCase<UseCase.None, LocationSupportRequest>() {

    override suspend fun run(params: LocationSupportRequest): Result<None> =
        lsRepository.requestNewConnection(params)
}