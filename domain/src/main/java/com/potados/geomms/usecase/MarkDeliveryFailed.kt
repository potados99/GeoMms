package com.potados.geomms.usecase

import com.potados.geomms.core.functional.Result
import com.potados.geomms.core.interactor.UseCase
import com.potados.geomms.repository.MessageRepository

class MarkDeliveryFailed(
    private val messageRepo: MessageRepository
) : UseCase<Unit, MarkDeliveryFailed.Params>() {

    data class Params(val id: Long, val resultCode: Int)

    override suspend fun run(params: Params): Result<Unit> =
        Result.of { messageRepo.markDeliveryFailed(params.id, params.resultCode) }
}