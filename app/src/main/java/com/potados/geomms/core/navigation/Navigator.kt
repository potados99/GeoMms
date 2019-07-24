package com.potados.geomms.core.navigation

import android.content.Context
import android.provider.Telephony
import com.potados.geomms.core.util.PermissionChecker
import com.potados.geomms.feature.message.ConversationActivity
import com.potados.geomms.feature.common.GiveMePermissionActivity
import com.potados.geomms.feature.common.MainActivity
import com.potados.geomms.feature.message.MakeMeDefaultAppActivity
import com.potados.geomms.feature.message.SmsThread

/**
 * Global activity navigator.
 */
class Navigator (private val permissionChecker: PermissionChecker){

    fun showMain(context: Context) {
        whenPossible(context) {
            it.startActivity(MainActivity.callingIntent(it))
        }
    }

    fun showConversationActivity(context: Context, smsThread: SmsThread) {
        whenPossible(context) {
            it.startActivity(
                ConversationActivity.callingIntent(it, smsThread)
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
        if (!permissionChecker.isAllGranted()) {
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