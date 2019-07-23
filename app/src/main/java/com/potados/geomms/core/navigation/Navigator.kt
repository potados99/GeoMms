package com.potados.geomms.core.navigation

import android.content.Context
import android.provider.Telephony
import com.potados.geomms.core.util.PermissionChecker
import com.potados.geomms.feature.activity.ConversationActivity
import com.potados.geomms.feature.activity.GiveMePermissionActivity
import com.potados.geomms.feature.activity.MainActivity
import com.potados.geomms.feature.activity.MakeMeDefaultAppActivity
import com.potados.geomms.feature.data.entity.SmsThread

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
                ConversationActivity.callingIntent(it).apply {
                    putExtra(ConversationActivity.ARG_SMS_THREAD, smsThread)
                }
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