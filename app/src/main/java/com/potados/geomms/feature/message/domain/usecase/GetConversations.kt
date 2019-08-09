package com.potados.geomms.feature.message.domain.usecase

import com.potados.geomms.core.interactor.UseCase
import com.potados.geomms.core.interactor.UseCase.None
import com.potados.geomms.feature.message.data.MessageRepository
import com.potados.geomms.feature.message.domain.Conversation

class GetConversations(
    private val messageRepository: MessageRepository
) : UseCase<List<Conversation>, None>() {

    override suspend fun buildObservable(params: None): Flowable<*> =
        messageRepository.getConversations()
}