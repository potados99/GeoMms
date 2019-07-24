package com.potados.geomms.feature.location.usecase

import com.potados.geomms.core.exception.Failure
import com.potados.geomms.core.functional.Either
import com.potados.geomms.core.interactor.UseCase
import com.potados.geomms.feature.location.data.LocationSupportConnection
import com.potados.geomms.feature.location.data.LocationSupportRepository

class GetConnections(
    private val lsRepository: LocationSupportRepository
) : UseCase<List<LocationSupportConnection>, UseCase.None>() {

    override suspend fun run(params: None): Either<Failure, List<LocationSupportConnection>> =
        lsRepository.getConnection()
}