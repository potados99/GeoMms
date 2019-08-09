package com.potados.geomms.feature.location.domain.usecase

import com.potados.geomms.core.interactor.UseCase
import com.potados.geomms.feature.location.data.LSRequest
import com.potados.geomms.feature.location.domain.LSService

class GetInboundRequests(
    private val service: LSService
) : UseCase<List<LSRequest>, UseCase.None>() {

    override suspend fun buildObservable(params: None): Flowable<*> =
        service.getInboundRequests()
}