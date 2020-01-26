/*
 * Copyright (C) 2019-2020 Song Byeong Jun <potados99@gmail.com>
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

package com.potados.geomms.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.potados.geomms.usecase.DeleteMessages
import org.koin.core.KoinComponent
import org.koin.core.inject

/**
 * Manifest registered.
 * Receive explicit intent only.
 *
 * Invoke [DeleteMessages].
 *
 * @see [DeleteMessages]
 */
class DeleteMessagesReceiver : BroadcastReceiver(), KoinComponent {

    private val deleteMessages: DeleteMessages by inject()

    override fun onReceive(context: Context, intent: Intent) {
        val pendingResult = goAsync()
        val threadId = intent.getLongExtra("threadId", 0)
        val messageIds = intent.getLongArrayExtra("messageIds")

        deleteMessages(DeleteMessages.Params(messageIds.toList(), threadId)) { pendingResult.finish() }
    }

}