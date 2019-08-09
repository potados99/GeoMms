package com.potados.geomms.feature.location.domain.usecase

import com.potados.geomms.core.interactor.UseCase
import com.potados.geomms.feature.location.data.LSRequest
import com.potados.geomms.feature.location.domain.LSService

class AcceptNewConnection(
    private val service: LSService
) : UseCase<UseCase.None, LSRequest>() {

    override suspend fun buildObservable(params: LSRequest): Flowable<*> =
        service.acceptNewConnection(params)
}