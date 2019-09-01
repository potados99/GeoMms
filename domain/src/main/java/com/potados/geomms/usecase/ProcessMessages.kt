package com.potados.geomms.usecase

import com.potados.geomms.functional.Result
import com.potados.geomms.interactor.UseCase
import com.potados.geomms.service.LocationSupportService

/**
 * Process geomms messages left unhandled in SMS DB
 */
class ProcessMessages(
    private val service: LocationSupportService
) : UseCase<Unit>() {

    override fun run(params: Unit): Result<*> = Result.of {
        service.processUnhandledMessages()
    }
}