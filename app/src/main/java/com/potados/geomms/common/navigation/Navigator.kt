package com.potados.geomms.common.navigation

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Telephony
import com.potados.geomms.base.FailableComponent
import com.potados.geomms.common.GiveMePermissionActivity
import com.potados.geomms.feature.compose.ComposeActivity
import com.potados.geomms.feature.location.InviteActivity
import com.potados.geomms.feature.main.MainActivity
import com.potados.geomms.feature.settings.SettingsActivity
import com.potados.geomms.util.Notify
import org.koin.core.KoinComponent
import timber.log.Timber

/**
 * Global activity navigator.
 */
class Navigator (
    private val context: Context,
    private val permissionManager: com.potados.geomms.manager.PermissionManager
) : FailableComponent(), KoinComponent {

    fun showMain() {
        whenPossible {
            startActivityWithFlag(MainActivity.callingIntent(it))
        }
    }

    fun showGuides() {
        // TODO show guides
        Notify(context).short("Sorry. Not implemented yet.")
    }

    fun showConversation(threadId: Long, query: String? = null) {
        whenPossible {
            startActivityWithFlag(
                ComposeActivity.callingIntent(it)
                    .putExtra("threadId", threadId)
                    .putExtra("query", query)
            )
        }
    }

    fun showCompose(body: String? = null, images: List<Uri>? = null) {
        whenPossible {
            val intent = ComposeActivity.callingIntent(it)
                .putExtra(Intent.EXTRA_TEXT, body)

            images?.takeIf { list -> list.isNotEmpty() }?.let {
                intent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, ArrayList(images))
            }

            startActivityWithFlag(intent)
        }
    }

    fun showDefaultSmsDialogIfNeeded() {
        if (Telephony.Sms.getDefaultSmsPackage(context) != context.packageName) {
            val intent = Intent(Telephony.Sms.Intents.ACTION_CHANGE_DEFAULT)
            if (Telephony.Sms.getDefaultSmsPackage(context) != context.packageName) {
                intent.putExtra(Telephony.Sms.Intents.EXTRA_PACKAGE_NAME, context.packageName)
            }
            startActivityWithFlag(intent)
        }
    }

    fun showInvite() {
        startActivityWithFlag(InviteActivity.callingIntent(context))
    }

    fun showSettings() {
        startActivityWithFlag(SettingsActivity.callingIntent(context))
    }

    private fun showGiveMePermission() {
        startActivityWithFlag(GiveMePermissionActivity.callingIntent(context))
    }

    private fun whenPossible(body: (Context) -> Unit) {
        if (!permissionManager.isAllGranted()) {
            /**
             * if not all permissions allowed.
             */
            showGiveMePermission()
        }
        else {
            body(context)
        }
    }

    private fun startActivityWithFlag(intent: Intent) {
        // on higher version of android
        context.startActivity(intent.apply { flags = Intent.FLAG_ACTIVITY_NEW_TASK })
    }

    // TODO
    fun showMedia(id: Long) {
        Timber.i("Show media: $id")
    }

    fun saveVcard(uri: Uri) {
        Timber.i("Save vcard: $uri")
    }
}