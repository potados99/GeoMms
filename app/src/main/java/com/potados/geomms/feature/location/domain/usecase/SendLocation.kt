package com.potados.geomms.feature.location.domain.usecase

import com.potados.geomms.core.interactor.UseCase
import com.potados.geomms.feature.location.data.LSConnection
import com.potados.geomms.feature.location.domain.LSService

class SendLocation(
    private val service: LSService
) : UseCase<UseCase.None, LSConnection>() {

    override suspend fun buildObservable(params: LSConnection): Flowable<*> =
        service.sendUpdate(params)
}