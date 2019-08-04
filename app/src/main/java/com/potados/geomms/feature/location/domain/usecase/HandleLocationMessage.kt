package com.potados.geomms.feature.location.domain.usecase

import com.potados.geomms.core.functional.Result
import com.potados.geomms.core.interactor.UseCase
import com.potados.geomms.feature.location.data.LSPacket
import com.potados.geomms.feature.location.domain.LSService

class HandleLocationMessage(
    private val service: LSService
) : UseCase<LSPacket, Pair<String, String>>() {

    override suspend fun run(params: Pair<String, String>): Result<LSPacket> =
        service.onPacketReceived(params.first, params.second)
}