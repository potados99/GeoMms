package com.potados.geomms.feature.message

import com.potados.geomms.core.exception.Failure
import com.potados.geomms.core.functional.Either
import com.potados.geomms.core.interactor.UseCase

class GetMessages(
    private val messageRepository: MessageRepository
) : UseCase<List<ShortMessage>, SmsThread>() {

    override suspend fun run(params: SmsThread): Either<Failure, List<ShortMessage>> =
        messageRepository.getMessagesFromSmsThread(params)
}