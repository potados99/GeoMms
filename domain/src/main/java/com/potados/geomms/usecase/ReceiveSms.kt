
package com.potados.geomms.usecase

import android.telephony.SmsMessage
import com.potados.geomms.functional.Result
import com.potados.geomms.interactor.UseCase
import com.potados.geomms.manager.NotificationManager
import com.potados.geomms.repository.ConversationRepository
import com.potados.geomms.repository.MessageRepository

class ReceiveSms(
    private val conversationRepo: ConversationRepository,
    private val messageRepo: MessageRepository,
    private val notificationManager: NotificationManager,
    private val updateBadge: UpdateBadge
) : UseCase<ReceiveSms.Params>() {

    class Params(val subId: Int, val messages: Array<SmsMessage>)

    override fun run(params: Params): Result<*> =
        Result.of {
            if (params.messages.isEmpty()) return@of

            val messages = params.messages
            val address = messages[0].displayOriginatingAddress
            val time = messages[0].timestampMillis
            val body: String = messages
                .mapNotNull { message -> message.displayMessageBody }
                .reduce { body, new -> body + new }

            messageRepo.insertReceivedSms(params.subId, address, body, time)
                ?.threadId
                ?.also { conversationRepo.updateConversations(it) }
                ?.also { conversationRepo.getOrCreateConversation(it) }
                ?.also { notificationManager.update(it) }
                ?.also { updateBadge(Unit) }
        }
}