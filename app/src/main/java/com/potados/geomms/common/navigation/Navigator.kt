package com.potados.geomms.common.navigation

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Telephony
import com.potados.geomms.R
import com.potados.geomms.base.FailableComponent
import com.potados.geomms.common.GiveMePermissionActivity
import com.potados.geomms.common.MakeMeDefaultAppActivity
import com.potados.geomms.feature.compose.ComposeActivity
import com.potados.geomms.feature.location.InviteActivity
import com.potados.geomms.feature.main.MainActivity
import com.potados.geomms.feature.settings.SettingsActivity
import com.potados.geomms.model.Conversation
import com.potados.geomms.usecase.DeleteConversations
import com.potados.geomms.util.Popup
import io.realm.Realm
import org.koin.core.KoinComponent
import org.koin.core.inject
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

    fun showAskDeleteConversation(threadId: Long) {
        val deleteConversations: DeleteConversations by inject()

        val conversation = Realm.getDefaultInstance()
            .where(Conversation::class.java)
            .findFirst() ?: return

        Popup(context)
            .withTitle(context.getString(R.string.delete_conversation_title))
            .withMessage(context.getString(R.string.delete_conversation_message, conversation.getTitle()))
            .withPositiveButton(context.getString(R.string.button_delete)) { _, _ ->
                deleteConversations(listOf(conversation.id))
            }
            .withNegativeButton(context.getString(R.string.button_cancel)) { _, _ ->
                // do nothing
            }
            .show()
    }

    fun showDefaultSmsDialog() {
        val intent = Intent(Telephony.Sms.Intents.ACTION_CHANGE_DEFAULT)
        if (Telephony.Sms.getDefaultSmsPackage(context) != context.packageName) {
            intent.putExtra(Telephony.Sms.Intents.EXTRA_PACKAGE_NAME, context.packageName)
        }
        startActivityWithFlag(intent)
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