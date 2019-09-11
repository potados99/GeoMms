package com.potados.geomms.feature.compose

import android.os.Bundle
import android.telephony.PhoneNumberUtils
import android.text.Editable
import android.text.TextWatcher
import android.view.*
import com.potados.geomms.R
import com.potados.geomms.common.base.BaseFragment
import com.potados.geomms.common.extension.*
import com.potados.geomms.common.navigation.Navigator
import com.potados.geomms.databinding.ComposeFragmentBinding
import com.potados.geomms.manager.PermissionManager
import com.potados.geomms.model.Contact
import com.potados.geomms.model.Message
import com.potados.geomms.model.PhoneNumber
import com.potados.geomms.repository.SyncRepository
import com.potados.geomms.usecase.DeleteMessages
import com.potados.geomms.util.Notify
import com.potados.geomms.util.Popup
import io.realm.RealmList
import kotlinx.android.synthetic.main.compose_fragment.view.*
import org.koin.core.inject
import timber.log.Timber
import java.util.*
import android.content.Intent
import android.net.Uri
import com.potados.geomms.model.Recipient

class ComposeFragment : BaseFragment() {

    override val optionMenuId: Int? = R.menu.compose

    private lateinit var composeViewModel: ComposeViewModel
    private lateinit var viewDataBinding: ComposeFragmentBinding

    private val chipsAdapter = ChipsAdapter()
    private val contactAdapter = ContactAdapter()
    private val messagesAdapter = MessagesAdapter()

    init {
        failables += this
        failables += chipsAdapter
        failables += contactAdapter
        failables += messagesAdapter
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        composeViewModel = getViewModel { activity?.intent?.let(::startWithIntent) }
        failables += composeViewModel.failables
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return ComposeFragmentBinding
            .inflate(inflater, container, false)
            .apply { vm = composeViewModel }
            .apply { lifecycleOwner = this@ComposeFragment }
            .apply { viewDataBinding = this }
            .apply { setSupportActionBar(toolbar = root.toolbar, title = false, upButton = true) }
            .apply { initializeView(root) }
            .root
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)

        val conversationIsNotNull = composeViewModel.conversation.value != null
        val singleRecipient = conversationIsNotNull && (composeViewModel.conversation.value?.recipients?.size == 1)

        with(menu) {
            setVisible(conversationIsNotNull)
            findItem(R.id.location)?.isVisible = singleRecipient
            findItem(R.id.call)?.isVisible = singleRecipient
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        super.onOptionsItemSelected(item)

        when (item.itemId) {
            R.id.call -> {
                getFirstRecipient()?.let {
                    startActivity(
                        Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + it.address))
                    )
                } ?: fail(R.string.fail_cannot_make_a_call_invalid_recipient, show = true)
            }
            R.id.location -> {
                Notify(context).short(R.string.notify_not_implemented)
            }
            R.id.info -> {
                Notify(context).short(R.string.notify_not_implemented)
            }
        }

        return true
    }

    override fun onDestroy() {
        super.onDestroy()
        composeViewModel.conversation.removeObservers(this)
    }

    private fun initializeView(view: View) {
        observe(composeViewModel.conversation) { conversation ->
            val conversationIsNull = (conversation == null)
            val conversationIsNotSetInitially = ((activity?.intent?.extras?.getLong("threadId") ?: 0L) == 0L)

            // Menu should be visible only when conversation is not null.
            getOptionsMenu()?.iterator()?.forEach { menuItem ->
                Timber.i("Set menu ${menuItem.itemId} visiblity: ${conversationIsNull.not()}.")
                menuItem.isVisible = !conversationIsNull
            }

            // Focus on edit text right after conversation is set.
            // If threadId extra does not exist in intent, it is the case.
            if (conversationIsNotSetInitially && !conversationIsNull) {
                Timber.i("Conversation is set. Request focus.")
                view.postDelayed({ view.message.showKeyboard() }, 200)
            }
        }

        with(view.chips) {
            adapter = chipsAdapter.apply {
                editText.setOnTextChanged {
                    val query = it.toString()

                    var contacts = composeViewModel.getContacts(query)

                    if (PhoneNumberUtils.isWellFormedSmsAddress(query)) {
                        val newAddress = PhoneNumberUtils.formatNumber(query, Locale.getDefault().country)
                        val newContact = Contact(numbers = RealmList(PhoneNumber(address = newAddress ?: query)))
                        contacts = listOf(newContact) + contacts
                    }

                    contactAdapter.data = contacts
                }
            }
        }

        with(view.contacts) {
            adapter = contactAdapter.apply {
                data = composeViewModel.getContacts()

                onContactClick = { composeViewModel.setConversationByContact(it) }
            }

            itemAnimator = null
        }

        with(view.messages) {
            setHasFixedSize(true)

            adapter = messagesAdapter.apply {
                autoScrollToStart(this@with)
                emptyView = view.messages_empty

                onMessageClick = { true /* TODO */ }
                onMessageLongClick = { composeViewModel.showMessageDeletionConfirmation(activity, it) }
            }
        }

        with(view.send) {
            setOnClickListener { composeViewModel.sendMessageIfCan(activity) }
        }

        with(view.message) {
            view.send.isEnabled = false
            view.send.imageAlpha = 128

            addTextChangedListener(object: TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                }
                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                    with(view.send) {
                        isEnabled = length() > 0
                        imageAlpha = if (length() > 0) 255 else 128
                    }
                }
                override fun afterTextChanged(s: Editable?) {
                }
            })
        }

        with(view.attach) {
            setOnClickListener {
                Notify(context).short(R.string.notify_not_implemented)
            }
        }
    }

    private fun getFirstRecipient(): Recipient? {
        return composeViewModel.conversation.value?.recipients?.get(0)
    }

}