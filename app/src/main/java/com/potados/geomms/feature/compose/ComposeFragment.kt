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
import com.potados.geomms.model.PhoneNumber
import com.potados.geomms.repository.SyncRepository
import com.potados.geomms.util.Notify
import io.realm.RealmList
import kotlinx.android.synthetic.main.compose_fragment.view.*
import org.koin.core.inject
import timber.log.Timber
import java.util.*


class ComposeFragment : BaseFragment() {

    override val optionMenuId: Int? = R.menu.compose

    private val permissionManager: PermissionManager by inject()
    private val navigator: Navigator by inject()
    private val syncRepo: SyncRepository by inject()

    private lateinit var composeViewModel: ComposeViewModel
    private lateinit var viewDataBinding: ComposeFragmentBinding

    private lateinit var chipsAdapter: ChipsAdapter
    private lateinit var contactAdapter: ContactAdapter
    private lateinit var messagesAdapter: MessagesAdapter

    init {
        failables += this
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        chipsAdapter = ChipsAdapter(context!!)
        messagesAdapter = MessagesAdapter(context!!)
        composeViewModel = getViewModel { activity?.intent?.let(::startWithIntent) }
        contactAdapter = ContactAdapter(composeViewModel::setConversationByContact)

        failables += composeViewModel.failables
        failables += chipsAdapter
        failables += contactAdapter
        failables += messagesAdapter
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

        menu.setVisible(composeViewModel.conversation.value != null)
    }

    override fun onDestroy() {
        super.onDestroy()
        composeViewModel.conversation.removeObservers(this)
    }

    private fun initializeView(view: View) {
        observe(composeViewModel.conversation) { conversation ->
            Timber.i("%s menu", if (conversation != null) "Show" else "Hide")
            getOptionsMenu()?.iterator()?.forEach { menuItem ->
                menuItem.isVisible = (conversation != null)
            } ?: Timber.i("Menu is null")

            if ((activity?.intent?.extras?.getLong("threadId") ?: 0L) == 0L) {
                conversation?.let {
                    Timber.i("Conversation is set. Request focus.")
                    view.postDelayed({ view.message.showKeyboard() }, 200)
                }
            }
        }

        with(chipsAdapter) {
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

        with(contactAdapter) {
            data = composeViewModel.getContacts()
        }

        with(view.chips) {
            adapter = chipsAdapter
        }

        with(view.contacts) {
            adapter = contactAdapter
            itemAnimator = null
        }

        with(view.messages) {
            setHasFixedSize(true)
            messagesAdapter.autoScrollToStart(this)
            messagesAdapter.emptyView = view.messages_empty
            adapter = messagesAdapter
        }

        with(view.send) {
            setOnClickListener {
                with(view.message) {
                    if (!permissionManager.isDefaultSms()) {
                        navigator.showDefaultSmsDialogIfNeeded()
                    }
                    else if (syncRepo.syncProgress.value is SyncRepository.SyncProgress.Running) {
                        Notify(context).short(R.string.notify_cannot_send_while_sync)
                    }
                    else {
                        composeViewModel.sendSms(text.toString())
                        text.clear()
                    }
                }
            }
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
    }
}