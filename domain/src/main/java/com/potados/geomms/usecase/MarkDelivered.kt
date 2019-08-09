package com.potados.geomms.usecase

import com.potados.geomms.functional.Result
import com.potados.geomms.interactor.UseCase
import com.potados.geomms.repository.MessageRepository

class MarkDelivered(
    private val messageRepo: MessageRepository
) : UseCase<Long>() {

    override suspend fun run(params: Long): Result<*> =
        Result.of{
            messageRepo.markDelivered(params)
        }
}
