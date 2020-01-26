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

package com.potados.geomms.feature.main

import androidx.fragment.app.FragmentActivity
import com.potados.geomms.common.base.BaseViewModel
import com.potados.geomms.common.navigation.Navigator
import com.potados.geomms.manager.PermissionManager
import com.potados.geomms.model.SyncLog
import com.potados.geomms.repository.SyncRepository
import io.realm.Realm
import org.koin.core.inject

class MainViewModel : BaseViewModel() {

    private val syncRepo: SyncRepository by inject()
    private val permissionManager: PermissionManager by inject()
    private val navigator: Navigator by inject()

    val syncEvent = syncRepo.syncEvent()

    /**
     * Binding elements
     */
    val defaultSmsState = permissionManager.isDefaultSmsLiveData()
    val syncState = syncRepo.syncProgress

    init {
        failables += syncRepo
        failables += permissionManager
        failables += navigator
    }

    /**
     * This method is recommended to be invoked by its owner childFragment,
     * in response of [syncEvent].
     *
     * This is supposed to be the only entry for sync in this whole application.
     */
    fun showSyncDialog(activity: FragmentActivity?) {
        navigator.showSyncDialog(activity)
    }

    fun showChangeDefaultAppDialog() {
        navigator.showDefaultSmsDialogIfNeeded()
    }
}