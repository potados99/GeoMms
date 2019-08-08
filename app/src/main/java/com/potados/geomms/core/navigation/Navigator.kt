package com.potados.geomms.core.navigation

import android.content.Context
import android.provider.Telephony
import com.potados.geomms.manager.PermissionManager
import com.potados.geomms.feature.message.representation.ConversationActivity
import com.potados.geomms.app.GiveMePermissionActivity
import com.potados.geomms.app.MainActivity
import com.potados.geomms.feature.message.MakeMeDefaultAppActivity
import com.potados.geomms.feature.message.domain.Conversation

/**
 * Global activity navigator.
 */
class Navigator (private val permissionManager: com.potados.geomms.manager.PermissionManager){

    fun showMain(context: Context) {
        whenPossible(context) {
            it.startActivity(MainActivity.callingIntent(it))
        }
    }

    fun showConversationActivity(context: Context, conversation: Conversation) {
        whenPossible(context) {
            it.startActivity(
                ConversationActivity.callingIntent(it, conversation.id)
            )
        }
    }

    private fun showGiveMePermission(context: Context) {
        context.startActivity(GiveMePermissionActivity.callingIntent(context))
    }

    private fun showMakeMeDefaultApp(context: Context) {
        context.startActivity(MakeMeDefaultAppActivity.callingIntent(context))
    }

    private fun whenPossible(context: Context, body: (Context) -> Unit) {
        if (!permissionManager.isAllGranted()) {
            /**
             * if not all permissions allowed.
             */
            showGiveMePermission(context)
        }
        else if (Telephony.Sms.getDefaultSmsPackage(context) != context.packageName) {
            /**
             * if this app is not a default messaging app.
             */
            showMakeMeDefaultApp(context)
        }
        else {
            body(context)
        }
    }

}