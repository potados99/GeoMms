
package com.potados.geomms.usecase

import com.potados.geomms.functional.Result
import com.potados.geomms.interactor.UseCase
import com.potados.geomms.manager.NotificationManager
import com.potados.geomms.repository.ConversationRepository
import com.potados.geomms.repository.MessageRepository

class MarkRead(
    private val conversationRepo: ConversationRepository,
    private val messageRepo: MessageRepository,
    private val notificationManager: NotificationManager,
    private val updateBadge: UpdateBadge
) : UseCase<List<Long>>() {

    override fun run(params: List<Long>): Result<*> =
        Result.of {
            params.toLongArray()
                .also { messageRepo.markRead(*it) }
                .also { conversationRepo.updateConversations(*it) }
                .also { it.forEach(notificationManager::update) }
                .also { updateBadge(Unit) }
        }
}