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

import android.net.Uri
import com.potados.geomms.functional.Result
import com.potados.geomms.interactor.UseCase
import com.potados.geomms.manager.ActiveConversationManager
import com.potados.geomms.manager.MyNotificationManager
import com.potados.geomms.repository.ConversationRepository
import com.potados.geomms.repository.MessageRepository
import com.potados.geomms.repository.SyncRepository

/**
 * Do additional things to receive an MMS.
 *
 * What happens when an MMS arrives:
 *  1. [MmsReceiver] notified.
 *  2. [MmsReceivedReceiver] triggered.
 *  3. [ReceiveMms] executed.
 */
class ReceiveMms(
    private val activeConversationManager: ActiveConversationManager,
    private val conversationRepo: ConversationRepository,
    private val syncRepo: SyncRepository,
    private val messageRepo: MessageRepository,
    private val notificationManager: MyNotificationManager,
    private val updateBadge: UpdateBadge
) : UseCase<Uri>() {

    override fun run(params: Uri): Result<*> =
        Result.of {
            val message = syncRepo.syncMessage(params) ?: return@of

            message.threadId
                .also { if (activeConversationManager.getActiveConversation() == it) { messageRepo.markRead(it) } }
                .also { conversationRepo.updateConversations(it) }
                .also { conversationRepo.getOrCreateConversation(it) }
                .also { notificationManager.updateThread(it) }
                .also { updateBadge(Unit) }
        }
}