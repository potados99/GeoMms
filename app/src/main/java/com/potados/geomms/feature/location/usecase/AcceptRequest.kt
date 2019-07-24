package com.potados.geomms.feature.location.usecase

import com.potados.geomms.core.exception.Failure
import com.potados.geomms.core.functional.Either
import com.potados.geomms.core.interactor.UseCase
import com.potados.geomms.feature.location.data.LocationSupportRepository
import com.potados.geomms.feature.location.data.LocationSupportRequest

class AcceptRequest(
    private val lsRepository: LocationSupportRepository
) : UseCase<UseCase.None, LocationSupportRequest>() {

    override suspend fun run(params: LocationSupportRequest): Either<Failure, None> =
        lsRepository.acceptRequest(params)
}