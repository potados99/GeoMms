package com.potados.geomms.common.navigation

import android.content.Context
import android.net.Uri
import android.provider.Telephony
import com.potados.geomms.common.GiveMePermissionActivity
import com.potados.geomms.common.MainActivity
import com.potados.geomms.feature.compose.ComposeActivity
import com.potados.geomms.common.MakeMeDefaultAppActivity
import com.potados.geomms.model.Conversation
import timber.log.Timber

/**
 * Global activity navigator.
 */
class Navigator (
    private val context: Context,
    private val permissionManager: com.potados.geomms.manager.PermissionManager
){

    fun showMain() {
        whenPossible {
            it.startActivity(MainActivity.callingIntent(it))
        }
    }

    fun showComposeActivity(conversation: Conversation) {
        whenPossible {
            it.startActivity(
                ComposeActivity.callingIntent(it, conversation.id)
            )
        }
    }

    private fun showGiveMePermission() {
        context.startActivity(GiveMePermissionActivity.callingIntent(context))
    }

    private fun showMakeMeDefaultApp() {
        context.startActivity(MakeMeDefaultAppActivity.callingIntent(context))
    }

    private fun whenPossible(body: (Context) -> Unit) {
        if (!permissionManager.isAllGranted()) {
            /**
             * if not all permissions allowed.
             */
            showGiveMePermission()
        }
        else if (Telephony.Sms.getDefaultSmsPackage(context) != context.packageName) {
            /**
             * if this app is not a default messaging app.
             */
            showMakeMeDefaultApp()
        }
        else {
            body(context)
        }
    }

    // TODO
    fun showMedia(id: Long) {
        Timber.i("show media: $id")
    }

    fun saveVcard(uri: Uri) {
        Timber.i("save vcard: $uri")
    }
}