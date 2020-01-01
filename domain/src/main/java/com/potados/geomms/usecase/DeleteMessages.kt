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

import com.potados.geomms.functional.Result
import com.potados.geomms.interactor.UseCase
import com.potados.geomms.manager.MyNotificationManager
import com.potados.geomms.repository.ConversationRepository
import com.potados.geomms.repository.MessageRepository

/**
 * Delete given messages.
 */
class DeleteMessages(
    private val conversationRepo: ConversationRepository,
    private val messageRepo: MessageRepository,
    private val notificationManager: MyNotificationManager,
    private val updateBadge: UpdateBadge
) : UseCase<DeleteMessages.Params>() {

    data class Params(val messageIds: List<Long>, val threadId: Long? = null)

    override fun run(params: Params): Result<*> =
        Result.of {
            params.messageIds.toLongArray()
                .also { messageIds -> messageRepo.deleteMessages(*messageIds) }                 // delete the messages
                .also { params.threadId?.let { conversationRepo.updateConversations(it) } }     // updateThread the conversation
                .also { params.threadId?.let(notificationManager::updateThread) }               // remove notifications on the conversation
                .also { updateBadge(Unit) }                                                     // updateThread the badge
        }
}