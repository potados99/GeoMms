package com.potados.geomms.feature.location

import android.content.Intent
import android.os.Bundle
import android.telephony.PhoneNumberUtils
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.potados.geomms.R
import com.potados.geomms.base.Failable
import com.potados.geomms.common.base.BaseFragment
import com.potados.geomms.common.extension.getViewModel
import com.potados.geomms.common.extension.setOnTextChanged
import com.potados.geomms.common.extension.setSupportActionBar
import com.potados.geomms.databinding.InviteFragmentBinding
import com.potados.geomms.feature.compose.ChipsAdapter
import com.potados.geomms.feature.compose.ContactAdapter
import com.potados.geomms.feature.location.MapFragment.Companion.ACTION_SET_ADDRESS
import com.potados.geomms.feature.location.MapFragment.Companion.EXTRA_ADDRESS
import com.potados.geomms.model.Contact
import com.potados.geomms.model.PhoneNumber
import io.realm.RealmList
import kotlinx.android.synthetic.main.invite_fragment.view.*
import org.koin.core.KoinComponent
import java.util.*

class InviteFragment :
    BaseFragment(),
    KoinComponent,
    ContactAdapter.ContactClickListener{

    private lateinit var inviteViewModel: InviteViewModel
    private lateinit var viewDataBinding: InviteFragmentBinding

    private val chipsAdapter = ChipsAdapter()
    private val contactAdapter = ContactAdapter(this)

    private var textWatcher: TextWatcher? = null

    init {
        failables += this
        failables += chipsAdapter
        failables += contactAdapter
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        inviteViewModel = getViewModel()
        failables += inviteViewModel.failables
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return InviteFragmentBinding
            .inflate(inflater, container, false)
            .apply { viewDataBinding = this }
            .apply { setSupportActionBar(toolbar = root.toolbar, title = false, upButton = true) }
            .apply { initializeView(root) }
            .root
    }

    override fun onDestroy() {
        super.onDestroy()

        chipsAdapter.editText.removeTextChangedListener(textWatcher)
    }

    private fun initializeView(view: View) {
        with(chipsAdapter) {
            textWatcher = editText.setOnTextChanged {
                val query = it.toString()

                var contacts = inviteViewModel.getContacts(query)

                if (PhoneNumberUtils.isWellFormedSmsAddress(query)) {
                    val newAddress = PhoneNumberUtils.formatNumber(query, Locale.getDefault().country)
                    val newContact = Contact(numbers = RealmList(PhoneNumber(address = newAddress ?: query)))
                    contacts = listOf(newContact) + contacts
                }

                contactAdapter.data = contacts
            }
        }

        with(contactAdapter) {
            data = inviteViewModel.getContacts()
        }

        with(view.chips) {
            adapter = chipsAdapter
        }

        with(view.contacts) {
            adapter = contactAdapter
        }
    }

    override fun onContactClick(contact: Contact) {
        val address= contact.numbers[0]?.address

        if (address == null) {
            fail(R.string.fail_cannot_select_address_not_exist, show = true)
        } else {
            context?.sendBroadcast(Intent(ACTION_SET_ADDRESS).putExtra(EXTRA_ADDRESS, address))
        }

        activity?.finish()
    }
}