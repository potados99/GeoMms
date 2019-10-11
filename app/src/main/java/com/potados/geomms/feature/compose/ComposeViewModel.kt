/*
 * Copyright (C) 2019 Song Byeong Jun <potados99@gmail.com>
 *
 * This file is part of GeoMms.
 *
 * GeoMms is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * GeoMms is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with GeoMms.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.potados.geomms.feature.compose

import android.content.Intent
import android.net.Uri
import android.telephony.PhoneNumberUtils
import androidx.databinding.ObservableField
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.MutableLiveData
import com.potados.geomms.R
import com.potados.geomms.common.base.BaseViewModel
import com.potados.geomms.common.navigation.Navigator
import com.potados.geomms.feature.location.MapFragment
import com.potados.geomms.feature.main.MainActivity
import com.potados.geomms.filter.ContactFilter
import com.potados.geomms.functional.Result
import com.potados.geomms.manager.ActiveConversationManager
import com.potados.geomms.manager.PermissionManager
import com.potados.geomms.model.*
import com.potados.geomms.repository.ContactRepository
import com.potados.geomms.repository.ConversationRepository
import com.potados.geomms.repository.MessageRepository
import com.potados.geomms.repository.SyncRepository
import com.potados.geomms.service.LocationSupportService
import com.potados.geomms.usecase.DeleteMessages
import com.potados.geomms.usecase.MarkRead
import com.potados.geomms.usecase.SendMessage
import com.potados.geomms.util.Notify
import com.potados.geomms.util.Popup
import io.realm.RealmList
import io.realm.RealmResults
import org.koin.core.KoinComponent
import org.koin.core.inject
import timber.log.Timber
import java.util.*

class ComposeViewModel : BaseViewModel(), KoinComponent {

    // Service
    private val service: LocationSupportService by inject()

    // Use case
    private val sendMessage: SendMessage by inject()
    private val markRead: MarkRead by inject()
    private val deleteMessage: DeleteMessages by inject()

    // Manager
    private val activeConversationManager: ActiveConversationManager by inject()
    private val conversationRepo: ConversationRepository by inject()
    private val messageRepo: MessageRepository by inject()
    private val permissionManager: PermissionManager by inject()
    private val navigator: Navigator by inject()

    // Repository
    private val contactRepo: ContactRepository by inject()
    private val syncRepo: SyncRepository by inject()

    // Filter
    private val contactFilter: ContactFilter by inject()

    // Parameters
    private lateinit var sharedText: String
    private lateinit var attachments: Attachments

    // Conversation data
    val conversation = MutableLiveData<Conversation>()      // null on empty
    val messages = MutableLiveData<RealmResults<Message>>() // null on empty

    // Binding element
    val messageText = ObservableField<String>()

    private var mThreadId: Long = 0L

    init {
        failables += this
        failables += activeConversationManager
        failables += conversationRepo
        failables += syncRepo
        failables += permissionManager
        failables += navigator
        failables += messageRepo
        failables += contactRepo
        failables += contactFilter
    }

    // Setup childFragment with Intent of parent Activity.
    fun startWithIntent(intent: Intent) {

        // Not a direct call from Fragment, but it's okay.
        start()

        mThreadId = intent.extras?.getLong("threadId") ?: 0L

        if (mThreadId == 0L) {
            // conversation is not set.
            sharedText = ""
            attachments = Attachments(listOf())

        } else {
            // conversation is set.
            // handle messages, shared text and attachments.

            conversation.value = conversationRepo.getConversation(mThreadId)
            messages.value = messageRepo.getMessages(mThreadId)

            // To be clear...
            activeConversationManager.setActiveConversation(mThreadId)

            markRead(listOf(mThreadId)) {
                if (it is Result.Error) {
                    fail(R.string.fail_mark_read, show = true)
                }
            }

            sharedText = intent.extras?.getString(Intent.EXTRA_TEXT) ?: ""

            val sharedImages = mutableListOf<Uri>()
            intent.getParcelableExtra<Uri>(Intent.EXTRA_STREAM)?.let(sharedImages::add)
            intent.getParcelableArrayListExtra<Uri>(Intent.EXTRA_STREAM)?.let(sharedImages::addAll)
            attachments = Attachments(sharedImages.map { Attachment.Image(it) })
        }

        Timber.i("View model started.")
    }

    fun setConversationByContact(contact: Contact) {
        val address = contact.numbers[0]?.address

        if (address == null) {
            fail(R.string.fail_cannot_select_address_not_exist, show = true)
            return
        }

        val derivedConversation = conversationRepo.getOrCreateConversation(address)

        if (derivedConversation == null) {
            fail(R.string.fail_get_or_create_conversation, show = true)
            return
        }

        conversation.value = derivedConversation
        messages.value = messageRepo.getMessages(derivedConversation.id)
    }

    fun getContacts(query: String = ""): List<Contact> {
        val contacts = contactRepo.getUnmanagedContacts()

        if (contacts == null) {
            fail(R.string.fail_get_contacts, true)
            return listOf()
        }

        return contacts.filter { contactFilter.filter(it, query) }
    }

    fun showMessageDeletionConfirmation(activity: FragmentActivity?, message: Message) {
        Popup(activity)
            .withTitle(R.string.title_delete_message)
            .withMessage(R.string.dialog_ask_delete_message)
            .withPositiveButton(R.string.button_delete) {
                deleteMessage(DeleteMessages.Params(listOf(message.id), conversation.value?.id))
            }
            .withNegativeButton(R.string.button_cancel)
            .show()
    }

    fun sendMessageIfCan(activity: FragmentActivity?) {
        if (!permissionManager.isDefaultSms()) {
            navigator.showDefaultSmsDialogIfNeeded()
        }
        else if (syncRepo.syncProgress.value is SyncRepository.SyncProgress.Running) {
            Notify(activity).short(R.string.notify_cannot_send_while_sync)
        }
        else {
            val conversation = conversation.value ?: return

            val params = SendMessage.Params(
                subId = -1,
                threadId = conversation.id,
                addresses = conversation.recipients.map { recipient -> recipient.address },
                body = messageText.get().orEmpty(),
                attachments = attachments
            )

            sendMessage(params) {
                it.onError { fail(R.string.fail_send_message, show = true) }.onSuccess { messageText.set("") }
            }
        }
    }

    fun setActiveConversation() {
        activeConversationManager.setActiveConversation(mThreadId)
    }

    fun unsetActiveConversation() {
        activeConversationManager.setActiveConversation(null)
    }

    fun getSearchResult(query: CharSequence? = ""): List<Contact> {
        val queryString = (query ?: "").toString()

        var contacts = getContacts(queryString)

        if (PhoneNumberUtils.isWellFormedSmsAddress(queryString)) {
            val newAddress = PhoneNumberUtils.formatNumber(queryString, Locale.getDefault().country)
            val newContact = Contact(numbers = RealmList(PhoneNumber(address = newAddress ?: queryString)))
            contacts = listOf(newContact) + contacts
        }

        return contacts
    }

    fun call(activity: FragmentActivity?) {
        getAddress(0)?.let {
            activity?.startActivity(
                Intent(Intent.ACTION_DIAL, Uri.parse("tel:$it"))
            )
        } ?: fail(R.string.fail_cannot_make_a_call_invalid_recipient, show = true)
    }

    fun showOnMap(activity: FragmentActivity?) {
        val address = getAddress(0)

        if (address == null) {
            fail(R.string.fail_cannot_share_invalid_recipient, show = true)
            return
        }

        if (service.canInvite(address)) {
            Popup(activity)
                .withTitle(R.string.title_share_location)
                .withMessage(R.string.dialog_share_location_with, getTitle())
                .withPositiveButton(R.string.button_ok) {
                    activity?.let {
                        // Finish this
                        it.finish()

                        // Show map fragment.
                        it.sendBroadcast(
                            Intent(MainActivity.ACTION_SHOW_MAP)
                        )

                        // Do invite.
                        it.sendBroadcast(
                            Intent(MapFragment.ACTION_SET_ADDRESS).putExtra(MapFragment.EXTRA_ADDRESS, address)
                        )
                    }
                }
                .withNegativeButton(R.string.button_no)
                .show()
        } else {
            activity?.let {
                // Finish this
                it.finish()

                // Show map fragment.
                it.sendBroadcast(
                    Intent(MainActivity.ACTION_SHOW_MAP)
                )
            }
        }
    }

    private fun getAddress(recipientIndex: Int = 0): String? {
        return conversation.value?.recipients?.get(recipientIndex)?.address
    }

    private fun getTitle(): String? {
        return conversation.value?.getTitle()
    }

    override fun onCleared() {
        super.onCleared()

        // To be clear...
        unsetActiveConversation()
    }
}

