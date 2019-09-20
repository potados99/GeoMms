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

package com.potados.geomms.repository

import android.net.Uri
import androidx.lifecycle.LiveData
import com.potados.geomms.model.Message
import timber.log.Timber

abstract class SyncRepository : Repository() {

    sealed class SyncProgress {
        object Idle : SyncProgress() {
            init {
                Timber.v("Idle progress created")
            }
        }
        data class Running(val max: Int, val progress: Int, val indeterminate: Boolean) : SyncProgress() {
            init {
                Timber.v("Running progress[$progress] created")
            }
        }
    }

    abstract val syncProgress: LiveData<SyncProgress>

    /**
     * Sum of messages count, conversations count, and recipients count.
     */
    abstract val rows: Int

    abstract fun syncEvent(): LiveData<Boolean>

    /**
     * Not directly sync messages.
     * Ask user by setting syncEvent and then
     * syncMessages will be called in fragment, not here.
     */
    abstract fun triggerSyncMessages()

    abstract fun syncMessages(dateFrom: Long = 0)

    abstract fun syncMessage(uri: Uri): Message?

    abstract fun syncContacts()

    /**
     * Syncs a single contact to the Realm
     *
     * Return false if the contact couldn't be found
     */
    abstract fun syncContact(address: String): Boolean?

}