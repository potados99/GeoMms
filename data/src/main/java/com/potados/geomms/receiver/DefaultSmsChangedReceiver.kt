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

package com.potados.geomms.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.Telephony
import androidx.annotation.RequiresApi
import com.potados.geomms.manager.PermissionManager
import com.potados.geomms.repository.SyncRepository
import com.potados.geomms.usecase.ProcessMessages
import com.potados.geomms.usecase.SyncMessages
import org.koin.core.KoinComponent
import org.koin.core.inject
import timber.log.Timber

/**
 * Manifest registered.
 * Receive both explicit intent and
 * implicit intent with action of [android.provider.action.DEFAULT_SMS_PACKAGE_CHANGED].
 *
 * Invoke [SyncMessages], and [PermissionManager::refresh].
 *
 * @see [SyncMessages]
 */
class DefaultSmsChangedReceiver : BroadcastReceiver(), KoinComponent {

    private val syncRepository: SyncRepository by inject()

    private val permissionManager: PermissionManager by inject()

    @RequiresApi(Build.VERSION_CODES.N)
    override fun onReceive(context: Context, intent: Intent) {
        Timber.i("Default sms app changed")

        if (intent.getBooleanExtra(Telephony.Sms.Intents.EXTRA_IS_DEFAULT_SMS_APP, false)) {
            val pendingResult = goAsync()
            permissionManager.refresh() // Update default sms packet name

            // Just trigger sync action and let user decide it.
            // If no observer exists for this event, this does nothing.
            // That can be a problem when user changed default sms app and done somthing,
            // and then return the default app back to this app, without this app detecting
            // need to sync again.
            syncRepository.triggerSyncMessages()
        }
    }
}