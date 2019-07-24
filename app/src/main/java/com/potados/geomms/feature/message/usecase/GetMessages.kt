package com.potados.geomms.feature.message.usecase

import com.potados.geomms.core.exception.Failure
import com.potados.geomms.core.functional.Either
import com.potados.geomms.core.interactor.UseCase
import com.potados.geomms.feature.message.data.MessageRepository
import com.potados.geomms.feature.message.data.ShortMessage
import com.potados.geomms.feature.message.data.SmsThread

class GetMessages(
    private val messageRepository: MessageRepository
) : UseCase<List<ShortMessage>, SmsThread>() {

    override suspend fun run(params: SmsThread): Either<Failure, List<ShortMessage>> =
        messageRepository.getMessagesFromSmsThread(params)
}