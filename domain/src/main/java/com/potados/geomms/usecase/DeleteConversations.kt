package com.potados.geomms.usecase

import com.potados.geomms.functional.Result
import com.potados.geomms.interactor.UseCase
import com.potados.geomms.manager.NotificationManager
import com.potados.geomms.repository.ConversationRepository

class DeleteConversations(
    private val conversationRepo: ConversationRepository,
    private val notificationManager: NotificationManager,
    private val updateBadge: UpdateBadge
) : UseCase<List<Long>>() {

    override fun run(params: List<Long>): Result<*> =
        Result.of {
            params.toLongArray()
                .also { threadIds -> conversationRepo.deleteConversations(*threadIds) }
                .also { threadIds -> threadIds.forEach(notificationManager::update) }
                .also { updateBadge(Unit) }
        }
}