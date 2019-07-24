package com.potados.geomms.feature.message.usecase

import com.potados.geomms.core.exception.Failure
import com.potados.geomms.core.functional.Either
import com.potados.geomms.core.interactor.UseCase
import com.potados.geomms.core.interactor.UseCase.None
import com.potados.geomms.feature.message.data.MessageRepository
import com.potados.geomms.feature.message.data.SmsThread

class GetConversations(
    private val messageRepository: MessageRepository
) : UseCase<List<SmsThread>, None>() {

    override suspend fun run(params: None): Either<Failure, List<SmsThread>> =
        messageRepository.getSmsThreads()
}