package com.potados.geomms.feature.message.domain.usecase

import com.potados.geomms.core.functional.Result
import com.potados.geomms.core.interactor.UseCase
import com.potados.geomms.feature.message.data.MessageRepository
import com.potados.geomms.feature.message.domain.Conversation
import com.potados.geomms.feature.message.domain.Sms

class GetMessages(
    private val messageRepository: MessageRepository
) : UseCase<List<Sms>, Conversation>() {

    override suspend fun run(params: Conversation): Result<List<Sms>> =
        messageRepository.getMessagesInConversation(params)
}