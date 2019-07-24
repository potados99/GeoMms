package com.potados.geomms.feature.location.usecase

import com.potados.geomms.core.exception.Failure
import com.potados.geomms.core.functional.Either
import com.potados.geomms.core.interactor.UseCase
import com.potados.geomms.feature.location.data.LocationSupportRepository
import com.potados.geomms.feature.location.data.LocationSupportRequest

class GetIncommingRequests(
    private val lsRepository: LocationSupportRepository
) : UseCase<List<LocationSupportRequest>, UseCase.None>() {

    override suspend fun run(params: None): Either<Failure, List<LocationSupportRequest>> =
        lsRepository.getIncommingRequests()
}