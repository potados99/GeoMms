package com.potados.geomms.feature.usecase

import com.potados.geomms.core.exception.Failure
import com.potados.geomms.core.functional.Either
import com.potados.geomms.core.interactor.UseCase
import com.potados.geomms.feature.data.entity.SmsThread
import com.potados.geomms.feature.data.repository.MessageRepository

class ReadConversation(
    private val messageRepository: MessageRepository
) : UseCase<UseCase.None, SmsThread>() {

    override suspend fun run(params: SmsThread): Either<Failure, None> =
        messageRepository.readConversation(params)
}