package com.potados.geomms.feature.compose

import android.os.Bundle
import android.telephony.PhoneNumberUtils
import android.text.Editable
import android.text.TextWatcher
import android.view.*
import com.potados.geomms.R
import com.potados.geomms.base.Failable
import com.potados.geomms.common.base.BaseFragment
import com.potados.geomms.common.extension.*
import com.potados.geomms.common.navigation.Navigator
import com.potados.geomms.databinding.ComposeFragmentBinding
import com.potados.geomms.manager.PermissionManager
import com.potados.geomms.model.Contact
import com.potados.geomms.model.PhoneNumber
import io.realm.RealmList
import kotlinx.android.synthetic.main.compose_fragment.view.*
import org.koin.core.inject
import timber.log.Timber
import java.util.*

class ComposeFragment : BaseFragment() {

    override val optionMenuId: Int? = R.menu.compose

    private val permissionManager: PermissionManager by inject()
    private val navigator: Navigator by inject()

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

    private fun initializeView(view: View) {
        observe(composeViewModel.conversation) { conversation ->
            Timber.i("%s menu", if (conversation != null) "show" else "hide")
            getOptionsMenu()?.iterator()?.forEach { menuItem ->
                menuItem.isVisible = (conversation != null)
            } ?: Timber.i("Menu is null")
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
                    } else {
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