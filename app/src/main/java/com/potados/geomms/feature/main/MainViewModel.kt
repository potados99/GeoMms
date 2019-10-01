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

    override fun start() {
        super.start()

        syncIfNeeded()
    }

    /**
     * Sync messages if not done.
     */
    private fun syncIfNeeded() {
        if (isNotSyncedYet()) {
            syncRepo.triggerSyncMessages(SyncRepository.SyncEvent.EVENT_INITIAL)
        }
    }

    fun isNotSyncedYet(): Boolean {
        val lastSync = Realm.getDefaultInstance().use { realm -> realm.where(SyncLog::class.java)?.max("date") ?: 0 }
        return (lastSync == 0 &&
                permissionManager.isDefaultSms() &&
                permissionManager.hasReadSms() &&
                permissionManager.hasContacts())
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
}