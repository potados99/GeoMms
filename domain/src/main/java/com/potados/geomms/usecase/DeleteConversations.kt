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

class DeleteConversations(
    private val conversationRepo: ConversationRepository,
    private val notificationManager: MyNotificationManager,
    private val updateBadge: UpdateBadge
) : UseCase<List<Long>>() {

    override fun run(params: List<Long>): Result<*> =
        Result.of {
            params.toLongArray()
                .also { threadIds -> conversationRepo.deleteConversations(*threadIds) }
                .also { threadIds -> threadIds.forEach(notificationManager::updateThread) }
                .also { updateBadge(Unit) }
        }
}