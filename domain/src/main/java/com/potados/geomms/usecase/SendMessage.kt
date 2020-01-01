/*
 * Copyright (C) 2019 Song Byeong Jun <potados99@gmail.com>
 *
 * This file is part of GeoMms.
 *
 * GeoMms is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * GeoMms is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with GeoMms.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.potados.geomms.usecase

import android.content.Context
import com.potados.geomms.compat.TelephonyCompat
import com.potados.geomms.functional.Result
import com.potados.geomms.interactor.UseCase
import com.potados.geomms.model.Attachment
import com.potados.geomms.repository.ConversationRepository
import com.potados.geomms.repository.MessageRepository

/**
 * Send a message. MMS or SMS.
 *
 * Sent messages are saved to DB.
 * Feedback will be given, such as succeeded, failed, etc...
 */
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