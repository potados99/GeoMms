package com.potados.geomms.feature.compose

import android.content.Intent
import android.net.Uri
import androidx.lifecycle.MutableLiveData
import com.potados.geomms.base.Failable
import com.potados.geomms.common.base.BaseViewModel
import com.potados.geomms.filter.ContactFilter
import com.potados.geomms.functional.Result
import com.potados.geomms.manager.ActiveConversationManager
import com.potados.geomms.model.*
import com.potados.geomms.repository.ContactRepository
import com.potados.geomms.repository.ConversationRepository
import com.potados.geomms.repository.MessageRepository
import com.potados.geomms.usecase.MarkRead
import com.potados.geomms.usecase.SendMessage
import io.realm.RealmResults
import org.koin.core.KoinComponent
import org.koin.core.inject
import timber.log.Timber

/**
 * ConversationActivity를 보조할 뷰모델입니다.
 */
class ComposeViewModel : BaseViewModel(), KoinComponent {

    // Use case
    private val sendMessage: SendMessage by inject()
    private val markRead: MarkRead by inject()

    // Manager
    private val activeConversationManager: ActiveConversationManager by inject()
    private val conversationRepo: ConversationRepository by inject()
    private val messageRepo: MessageRepository by inject()

    // Repository
    private val contactRepo: ContactRepository by inject()

    // Filter
    private val contactFilter: ContactFilter by inject()

    // Parameters
    lateinit var sharedText: String
    lateinit var attatchments: Attachments

    // Conversation data
    val conversation = MutableLiveData<Conversation>()      // null on empty
    val messages = MutableLiveData<RealmResults<Message>>() // null on empty

    init {
        failables += this
        failables += activeConversationManager
        failables += conversationRepo
        failables += messageRepo
        failables += contactRepo
        failables += contactFilter
    }

    // Setup fragment with Intent of parent Activity.
    fun startWithIntent(intent: Intent) {

        // Not a direct call from Fragment, but it's okay.
        start()

        val threadId = intent.extras?.getLong("threadId") ?: 0L

        if (threadId == 0L) {
            // conversation is not set.
            sharedText = ""
            attatchments = Attachments(listOf())

        } else {
            // conversation is set.
            // handle messages, shared text and attachments.

            conversation.value = conversationRepo.getConversation(threadId)
            messages.value = messageRepo.getMessages(threadId)

            activeConversationManager.setActiveConversation(threadId)
            markRead(listOf(threadId)) {
                if (it is Result.Error) {
                    setFailure(Failable.Failure("Failed to mark as read.", true))
                }
            }

            sharedText = intent.extras?.getString(Intent.EXTRA_TEXT) ?: ""

            val sharedImages = mutableListOf<Uri>()
            intent.getParcelableExtra<Uri>(Intent.EXTRA_STREAM)?.let(sharedImages::add)
            intent.getParcelableArrayListExtra<Uri>(Intent.EXTRA_STREAM)?.let(sharedImages::addAll)
            attatchments = Attachments(sharedImages.map { Attachment.Image(it) })
        }

        Timber.i("viewmodel started.")
    }

    fun setConversationByContact(contact: Contact) {
        val address = contact.numbers[0]?.address ?: throw RuntimeException("check ContactAdapter.")

        val derivedConversation = conversationRepo.getOrCreateConversation(address)
            ?: throw RuntimeException("failed to get or create conversation")

        conversation.value = derivedConversation
        messages.value = messageRepo.getMessages(derivedConversation.id)
    }

    fun getContacts(query: String = ""): List<Contact> {
        val contacts = contactRepo.getUnmanagedContacts()?.filter {
            contactFilter.filter(it, query)
        }

        if (contacts == null) {
            setFailure(Failable.Failure("Failed to get contacts.", true))
            return listOf()
        }

        return contacts
    }

    /* 임시 TODO */
    fun sendSms(body: String) {
        val conversation = conversation.value ?: return

        val params = SendMessage.Params(
            subId = -1,
            threadId = conversation.id,
            addresses = conversation.recipients.map { recipient -> recipient.address },
            body = body,
            attachments = listOf()
        )

        sendMessage(params) {
            if (it is Result.Error) {
                setFailure(Failable.Failure("Failed to send SMS.", true))
            }
        }
    }
}

