package com.potados.geomms.common.navigation

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
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
            startActivityWithFlag(MainActivity.callingIntent(it))
        }
    }

    fun showComposeActivity(conversation: Conversation) {
        whenPossible {
            startActivityWithFlag(ComposeActivity.callingIntent(it, conversation.id))
        }
    }

    fun showDefaultSmsDialog() {
        val intent = Intent(Telephony.Sms.Intents.ACTION_CHANGE_DEFAULT)
        if (Telephony.Sms.getDefaultSmsPackage(context) != context.packageName) {
            intent.putExtra(Telephony.Sms.Intents.EXTRA_PACKAGE_NAME, context.packageName)
        }
        startActivityWithFlag(intent)
    }

    private fun showGiveMePermission() {
        startActivityWithFlag(GiveMePermissionActivity.callingIntent(context))
    }

    private fun showMakeMeDefaultApp() {
        startActivityWithFlag(MakeMeDefaultAppActivity.callingIntent(context))
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

    private fun startActivityWithFlag(intent: Intent) {
        // on higher version of android
        context.startActivity(intent.apply { flags = Intent.FLAG_ACTIVITY_NEW_TASK })
    }

    // TODO
    fun showMedia(id: Long) {
        Timber.i("show media: $id")
    }

    fun saveVcard(uri: Uri) {
        Timber.i("save vcard: $uri")
    }
}