package com.potados.geomms.usecase

import com.potados.geomms.core.functional.Result
import com.potados.geomms.core.interactor.UseCase
import timber.log.Timber

class UpdateBadge : UseCase<Unit, Unit>() {
    override suspend fun run(params: Unit): Result<Unit> =
        Result.of {
            Timber.d("Updated badge.")
            // TODO
        }
}