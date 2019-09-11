
package com.potados.geomms.usecase

import com.potados.geomms.functional.Result
import com.potados.geomms.interactor.UseCase
import com.potados.geomms.manager.NotificationManager
import com.potados.geomms.repository.ConversationRepository
import com.potados.geomms.repository.MessageRepository

class DeleteMessages(
    private val conversationRepo: ConversationRepository,
    private val messageRepo: MessageRepository,
    private val notificationManager: NotificationManager,
    private val updateBadge: UpdateBadge
) : UseCase<DeleteMessages.Params>() {

    data class Params(val messageIds: List<Long>, val threadId: Long? = null)

    override fun run(params: Params): Result<*> =
        Result.of {
            params.messageIds.toLongArray()
                .also { messageIds -> messageRepo.deleteMessages(*messageIds) }                 // delete the messages
                .also { params.threadId?.let { conversationRepo.updateConversations(it) } }     // update the conversation
                .also { params.threadId?.let(notificationManager::update) }                     // remove notifications on the conversation
                .also { updateBadge(Unit) }                                                     // update the badge
        }
}