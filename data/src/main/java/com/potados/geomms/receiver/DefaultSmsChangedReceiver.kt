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