package com.potados.geomms.feature.message.domain.usecase

import com.potados.geomms.core.functional.Result
import com.potados.geomms.core.interactor.UseCase
import com.potados.geomms.feature.message.data.ConversationEntity
import com.potados.geomms.feature.message.data.MessageRepository
import com.potados.geomms.feature.message.domain.Conversation

class ReadConversation(
    private val messageRepository: MessageRepository
) : UseCase<UseCase.None, Conversation>() {

    override suspend fun run(params: Conversation): Result<None> =
        messageRepository.markConversationAsRead(params)
}