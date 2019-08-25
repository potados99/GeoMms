/*
 * Copyright (C) 2017 Moez Bhatti <moez.bhatti@gmail.com>
 *
 * This file is part of QKSMS.
 *
 * QKSMS is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * QKSMS is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with QKSMS.  If not, see <http://www.gnu.org/licenses/>.
 */
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

    override suspend fun run(params: Params): Result<*> =
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