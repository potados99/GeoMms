package com.potados.geomms.usecase

import com.potados.geomms.functional.Result
import com.potados.geomms.interactor.UseCase
import com.potados.geomms.service.LocationSupportService

class SendUpdate(
    private val service: LocationSupportService
) : UseCase<Long>() {

    override fun run(params: Long): Result<*> =
        Result.of {
            service.sendUpdate(params)
        }
}