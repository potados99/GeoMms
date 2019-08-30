package com.potados.geomms.usecase

import android.content.Context
import com.potados.geomms.compat.TelephonyCompat
import com.potados.geomms.functional.Result
import com.potados.geomms.interactor.UseCase
import com.potados.geomms.model.Attachment
import com.potados.geomms.repository.ConversationRepository
import com.potados.geomms.repository.MessageRepository

class SendMessage(
    private val context: Context,
    private val conversationRepo: ConversationRepository,
    private val messageRepo: MessageRepository
) : UseCase<SendMessage.Params>() {

    data class Params(
        val subId: Int,
        val threadId: Long,
        val addresses: List<String>,
        val body: String,
        val attachments: List<Attachment> = listOf()
    )

    override fun run(params: Params): Result<*> =
        Result.of {
            if (params.addresses.isEmpty()) return@of

            val threadId = when (params.threadId) {
                0L -> TelephonyCompat.getOrCreateThreadId(context, params.addresses.toSet())
                else -> params.threadId
            }

            messageRepo.sendMessage(params.subId, threadId, params.addresses, params.body, params.attachments)

            val retrievedId = when (params.threadId) {
                // If the threadId wasn't provided, then it's probably because it doesn't exist in Realm.
                // Sync it now and get the id
                0L -> conversationRepo.getOrCreateConversation(params.addresses)?.id!!
                else -> params.threadId
            }
            
            conversationRepo.updateConversations(retrievedId)
            conversationRepo.markUnarchived(retrievedId)
        }
}