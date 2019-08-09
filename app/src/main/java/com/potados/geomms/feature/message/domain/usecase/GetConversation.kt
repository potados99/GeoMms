package com.potados.geomms.feature.message.domain.usecase

import com.potados.geomms.core.interactor.UseCase
import com.potados.geomms.feature.message.data.MessageRepository
import com.potados.geomms.feature.message.domain.Conversation

class GetConversation(
    private val messageRepository: MessageRepository
) : UseCase<Conversation, Long>() {

    override suspend fun buildObservable(params: Long): Flowable<*> =
        messageRepository.getConversationById(params)
}