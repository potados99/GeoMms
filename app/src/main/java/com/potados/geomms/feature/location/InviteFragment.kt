package com.potados.geomms.feature.location

import android.content.Intent
import android.os.Bundle
import android.telephony.PhoneNumberUtils
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.potados.geomms.common.base.BaseFragment
import com.potados.geomms.common.extension.getViewModel
import com.potados.geomms.common.extension.setSupportActionBar
import com.potados.geomms.databinding.InviteFragmentBinding
import com.potados.geomms.feature.compose.ChipsAdapter
import com.potados.geomms.feature.compose.ContactAdapter
import com.potados.geomms.model.Contact
import com.potados.geomms.model.PhoneNumber
import io.realm.RealmList
import kotlinx.android.synthetic.main.invite_fragment.view.*
import kotlinx.android.synthetic.main.invite_fragment.view.chips
import kotlinx.android.synthetic.main.invite_fragment.view.toolbar
import kotlinx.android.synthetic.main.map_fragment.view.*
import java.util.*

class InviteFragment : BaseFragment() {

    private lateinit var inviteViewModel: InviteViewModel
    private lateinit var viewDataBinding: InviteFragmentBinding

    private lateinit var chipsAdapter: ChipsAdapter
    private lateinit var contactAdapter: ContactAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        inviteViewModel = getViewModel()
        chipsAdapter = ChipsAdapter(context!!)
        contactAdapter = ContactAdapter {
            context!!.sendBroadcast(Intent("inviteContact").putExtra("address", it.numbers[0]!!.address))
            activity?.finish()
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return InviteFragmentBinding
            .inflate(inflater, container, false)
            .apply { viewDataBinding = this }
            .apply { setSupportActionBar(toolbar = root.toolbar, title = false, upButton = true) }
            .apply { initializeView(root) }
            .root
    }

    private fun initializeView(view: View) {
        with(chipsAdapter) {
            editText.addTextChangedListener(object: TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                }
                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                    // TODO very dirty! fix it!

                    val query = s.toString()

                    var contacts = inviteViewModel.getContacts(query)

                    if (PhoneNumberUtils.isWellFormedSmsAddress(query)) {
                        val newAddress = PhoneNumberUtils.formatNumber(query, Locale.getDefault().country)
                        val newContact = Contact(numbers = RealmList(PhoneNumber(address = newAddress ?: query)))
                        contacts = listOf(newContact) + contacts
                    }

                    contactAdapter.data = contacts
                }
                override fun afterTextChanged(s: Editable?) {
                }
            })
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
}