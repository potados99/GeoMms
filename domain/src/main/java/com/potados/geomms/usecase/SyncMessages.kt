/**
 * Copyright (C) 2019 Song Byeong Jun and original authors
 *
 * This file is part of GeoMms.
 *
 * This software makes use of third-party patent which belongs to
 * KANG MOON KYOU and LEE GWI BONG:
 * System and Method for sharing service of location information
 * 10-1235884-0000 (2013.02.15)
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

import com.potados.geomms.extension.elapsedTimeMillis
import com.potados.geomms.functional.Result
import com.potados.geomms.interactor.UseCase
import com.potados.geomms.repository.SyncRepository
import timber.log.Timber
import java.util.concurrent.TimeUnit

class SyncMessages(
    private val syncRepo: SyncRepository,
    private val clearAll: ClearAll,
    private val processMessages: ProcessMessages,
    private val updateBadge: UpdateBadge
) : UseCase<Long>() {

    override fun run(params: Long): Result<*> =
        Result.of {
            Timber.i("Will sync messages from date $params.")

            // Connections relay on Recipient, which will be deleted after sync.
            // Disconnect all connections before sync to prevent connection having
            // no recipient.
            clearAll(Unit) {
                it.onError { Timber.w("Failed to clear all.") }
            }

            Timber.i("Clear all.")

            val elapsedMillis = elapsedTimeMillis {
                syncRepo.syncMessages(params)
            }

            val elapsedSeconds = TimeUnit.MILLISECONDS.toSeconds(elapsedMillis)

            Timber.i("Completed sync in $elapsedSeconds seconds.")

            processMessages(Unit) {
                it.onError { Timber.w("Failed to process messages.") }
            }

            updateBadge(Unit)
        }
}