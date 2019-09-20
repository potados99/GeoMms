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
import com.potados.geomms.usecase.SyncContacts
import org.koin.core.KoinComponent
import org.koin.core.inject

/**
 * Manifest registered.
 * Receive both explicit intent and
 * implicit intent with action of [android.provider.action.DEFAULT_SMS_PACKAGE_CHANGED].
 *
 * Invoke [SyncContacts].
 *
 * @see [SyncContacts]
 */
class LocaleChangedReceiver : BroadcastReceiver(), KoinComponent {

    private val syncContacts: SyncContacts by inject()

    override fun onReceive(context: Context?, intent: Intent?) {
        val pendingResult = goAsync()
        syncContacts(Unit) { pendingResult.finish() }
    }
}