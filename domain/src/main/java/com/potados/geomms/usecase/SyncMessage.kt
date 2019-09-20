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