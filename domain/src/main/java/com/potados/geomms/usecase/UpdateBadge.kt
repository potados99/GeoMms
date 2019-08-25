package com.potados.geomms.usecase

import com.potados.geomms.functional.Result
import com.potados.geomms.interactor.UseCase
import timber.log.Timber

class UpdateBadge : UseCase<Unit>() {
    override suspend fun run(params: Unit): Result<*> =
        Result.of{
            Timber.i("TODO: Do update badge")
        }
}