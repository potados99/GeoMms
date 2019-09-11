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

    abstract fun syncMessages()

    abstract fun syncMessage(uri: Uri): Message?

    abstract fun syncContacts()

    /**
     * Syncs a single contact to the Realm
     *
     * Return false if the contact couldn't be found
     */
    abstract fun syncContact(address: String): Boolean?

}