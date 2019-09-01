package com.potados.geomms.usecase

import com.potados.geomms.functional.Result
import com.potados.geomms.interactor.UseCase
import com.potados.geomms.service.LocationSupportService
import org.koin.core.KoinComponent
import org.koin.core.inject

/**
 * Clear all geomms things.
 */
class ClearAll(
    private val service: LocationSupportService
) : UseCase<Unit>(), KoinComponent {

    override fun run(params: Unit) = Result.of {
        service.clearAll()
    }
}