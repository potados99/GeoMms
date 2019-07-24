package com.potados.geomms.feature.message

import com.potados.geomms.core.exception.Failure
import com.potados.geomms.core.functional.Either
import com.potados.geomms.core.interactor.UseCase

class SendSms(
    private val messageRepository: MessageRepository
) : UseCase<UseCase.None, SmsEntity>() {

    override suspend fun run(params: SmsEntity): Either<Failure, None> =
        messageRepository.sendSms(params, true)
}