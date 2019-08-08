package com.potados.geomms.usecase

import com.potados.geomms.core.functional.Result
import com.potados.geomms.core.interactor.UseCase
import com.potados.geomms.repository.MessageRepository

class MarkDelivered(
    private val messageRepo: MessageRepository
) : UseCase<Unit, Long>() {

    override suspend fun run(params: Long): Result<Unit> =
        Result.of { messageRepo.markDelivered(params) }
}
