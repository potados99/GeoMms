
package com.potados.geomms.usecase

import android.net.Uri
import com.potados.geomms.functional.Result
import com.potados.geomms.interactor.UseCase
import com.potados.geomms.manager.ActiveConversationManager
import com.potados.geomms.manager.NotificationManager
import com.potados.geomms.repository.ConversationRepository
import com.potados.geomms.repository.MessageRepository
import com.potados.geomms.repository.SyncRepository

class ReceiveMms(
    private val activeConversationManager: ActiveConversationManager,
    private val conversationRepo: ConversationRepository,
    private val syncRepo: SyncRepository,
    private val messageRepo: MessageRepository,
    private val notificationManager: NotificationManager,
    private val updateBadge: UpdateBadge
) : UseCase<Uri>() {

    override fun run(params: Uri): Result<*> =
        Result.of {
            val message = syncRepo.syncMessage(params) ?: return@of

            message.threadId
                .also { if (activeConversationManager.getActiveConversation() == it) { messageRepo.markRead(it) } }
                .also { conversationRepo.updateConversations(it) }
                .also { conversationRepo.getOrCreateConversation(it) }
                .also { notificationManager.update(it) }
                .also { updateBadge(Unit) }
        }
}