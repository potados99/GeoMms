package com.potados.geomms.feature.message

import com.potados.geomms.core.exception.Failure
import com.potados.geomms.core.functional.Either
import com.potados.geomms.core.interactor.UseCase
import com.potados.geomms.core.interactor.UseCase.None

class GetConversations(
    private val messageRepository: MessageRepository
) : UseCase<List<SmsThread>, None>() {

    override suspend fun run(params: None): Either<Failure, List<SmsThread>> =
        messageRepository.getSmsThreads()
}