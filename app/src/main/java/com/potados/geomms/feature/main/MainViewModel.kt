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