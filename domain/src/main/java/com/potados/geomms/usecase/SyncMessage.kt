package com.potados.geomms.usecase

import android.net.Uri
import com.potados.geomms.core.functional.Result
import com.potados.geomms.core.interactor.UseCase
import com.potados.geomms.repository.ConversationRepository
import com.potados.geomms.repository.SyncRepository

class SyncMessage(
    private val conversationRepo: ConversationRepository,
    private val syncManager: SyncRepository,
    private val updateBadge: UpdateBadge
) : UseCase<Unit, Uri>() {
    override suspend fun run(params: Uri): Result<Unit> =
        Result.of {
            syncManager.syncMessage(params)
                ?.let { conversationRepo.updateConversations(it.threadId) }
                ?.also { updateBadge(Unit) }
        }
}