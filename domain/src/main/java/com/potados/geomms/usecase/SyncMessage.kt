package com.potados.geomms.usecase

import android.net.Uri
import com.potados.geomms.functional.Result
import com.potados.geomms.interactor.UseCase
import com.potados.geomms.repository.ConversationRepository
import com.potados.geomms.repository.SyncRepository

class SyncMessage(
    private val conversationRepo: ConversationRepository,
    private val syncManager: SyncRepository,
    private val updateBadge: UpdateBadge
) : UseCase<Uri>() {

    override fun run(params: Uri): Result<*> =
        Result.of {
            syncManager.syncMessage(params)?.let { conversationRepo.updateConversations(it.threadId) }
            updateBadge(Unit)
        }
}