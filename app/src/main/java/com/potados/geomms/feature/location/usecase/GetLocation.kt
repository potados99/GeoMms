package com.potados.geomms.feature.location.usecase

import android.location.Location
import com.potados.geomms.core.exception.Failure
import com.potados.geomms.core.functional.Either
import com.potados.geomms.core.interactor.UseCase
import com.potados.geomms.feature.location.LocationSupportFailure
import com.potados.geomms.feature.location.data.LocationRepository

class GetLocation(
    private val locationRepository: LocationRepository
) : UseCase<Location, UseCase.None>() {

    override suspend fun run(params: None): Either<Failure, Location> =
        try {
            Either.Right(locationRepository.getCurrentLocation() ?: throw IllegalStateException())
        } catch (e: Exception) {
            Either.Left(LocationSupportFailure.LocationFailure())
        }
}