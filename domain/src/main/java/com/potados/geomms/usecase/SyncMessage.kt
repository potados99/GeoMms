package com.potados.geomms.usecase

import android.net.Uri
import com.potados.geomms.extension.mapNotNull
import com.potados.geomms.repository.ConversationRepository
import com.potados.geomms.repository.SyncRepository
import io.reactivex.Flowable

class SyncMessage(
    private val conversationRepo: ConversationRepository,
    private val syncManager: SyncRepository,
    private val updateBadge: UpdateBadge
) : UseCase<Uri>() {

    override fun buildObservable(params: Uri): Flowable<*> =
        Flowable.just(params)
            .mapNotNull { uri -> syncManager.syncMessage(uri) }
            .doOnNext { message -> conversationRepo.updateConversations(message.threadId) }
            .flatMap { updateBadge.buildObservable(Unit) }
}