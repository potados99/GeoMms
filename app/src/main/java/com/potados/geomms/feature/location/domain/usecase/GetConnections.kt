package com.potados.geomms.feature.location.domain.usecase

import com.potados.geomms.core.interactor.UseCase
import com.potados.geomms.feature.location.data.LSConnection
import com.potados.geomms.feature.location.domain.LSService

class GetConnections(
    private val service: LSService
) : UseCase<List<LSConnection>, UseCase.None>() {

    override suspend fun buildObservable(params: None): Flowable<*> =
        service.getConnections()
}