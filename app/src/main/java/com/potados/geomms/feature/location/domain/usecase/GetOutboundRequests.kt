package com.potados.geomms.feature.location.domain.usecase

import com.potados.geomms.core.functional.Result
import com.potados.geomms.core.interactor.UseCase
import com.potados.geomms.core.interactor.UseCase.None
import com.potados.geomms.feature.location.data.LSRequest
import com.potados.geomms.feature.location.domain.LSService

class GetOutboundRequests(
    private val service: LSService
) : UseCase<List<LSRequest>, None>() {

    override suspend fun run(params: None): Result<List<LSRequest>> =
        service.getOutboundRequests()
}