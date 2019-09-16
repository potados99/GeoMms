package com.potados.geomms.common.navigation

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Telephony
import androidx.fragment.app.FragmentActivity
import com.potados.geomms.R
import com.potados.geomms.base.FailableComponent
import com.potados.geomms.common.GiveMePermissionActivity
import com.potados.geomms.feature.compose.ComposeActivity
import com.potados.geomms.feature.location.invite.InviteActivity
import com.potados.geomms.feature.main.MainActivity
import com.potados.geomms.feature.settings.SettingsActivity
import com.potados.geomms.manager.PermissionManager
import com.potados.geomms.repository.SyncRepository
import com.potados.geomms.usecase.SyncMessages
import com.potados.geomms.util.Notify
import com.potados.geomms.util.Popup
import org.koin.core.KoinComponent
import timber.log.Timber
import java.util.*
import kotlin.collections.ArrayList

class Navigator (
    private val context: Context,
    private val permissionManager: PermissionManager,
    private val syncRepo: SyncRepository,
    private val syncMessages: SyncMessages
) : FailableComponent(), KoinComponent {

    fun showMain() {
        whenPossible {
            startActivityWithFlag(MainActivity.callingIntent(it))
        }
    }

    fun showGuides() {
        // TODO show guides
        Notify(context).short(R.string.notify_not_implemented)
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

    /**
     * This method is recommended to be invoked by its owner fragment,
     * in response of [syncEvent].
     *
     * This is supposed to be the only entry for sync in this whole application.
     *
     * Why this way?
     * We need to ask user if there are too many messages to sync.
     * The sync can happen everywhere, and the asking requires dialog, which needs an activity.
     * Then copy and past a dialog code everywhere? No.
     * This method is the only way to ask for a user to set date condition.
     * Anywhere with activity can call this.
     *
     * Other way to launch method other than directly calling is to trigger sync event in
     * the sync repository.
     * Once the event is emitted, its observer(mostly the ConversationsFragment)
     * will call this method.
     */
    fun showSyncDialog(activity: FragmentActivity?) {
        if (syncRepo.rows > 5) {
            var fromDate: Long = 0

            Popup(activity)
                .withTitle(R.string.title_sync_slow)
                .withMessage(R.string.dialog_ask_sync_all, syncRepo.rows)
                .withPositiveButton(R.string.button_confirm) {
                    Popup(activity)
                        .withTitle(R.string.title_choose_range)
                        .withSingleChoiceItems(R.array.sync_limits) {
                            fromDate = when (it) {
                                // Last month
                                0 -> Calendar.getInstance().apply {
                                    add(Calendar.MONTH, -1)
                                }.timeInMillis

                                // Last 6 months
                                1 -> Calendar.getInstance().apply {
                                    add(Calendar.MONTH, -6)
                                }.timeInMillis

                                // Last year
                                2 -> Calendar.getInstance().apply {
                                    add(Calendar.YEAR, -1)
                                }.timeInMillis

                                // All
                                3 -> 0

                                else -> throw RuntimeException("This is IMPOSSIBLE. Check your code.")
                            }
                        }
                        .withNegativeButton(R.string.button_cancel)
                        .withPositiveButton(R.string.button_sync) {
                            syncMessages(fromDate) {
                                it.either({
                                    Notify(context).short(R.string.notify_sync_completed)
                                }, {
                                    Notify(context).short(R.string.notify_sync_failed)
                                })
                            }
                        }
                        .show()
                }
                .show()
        } else {
            syncMessages(0) {
                it.either({
                    Notify(context).short(R.string.notify_sync_completed)
                }, {
                    Notify(context).short(R.string.notify_sync_failed)
                })
            }
        }
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