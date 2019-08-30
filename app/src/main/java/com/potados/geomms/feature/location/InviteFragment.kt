package com.potados.geomms.feature.location

import android.content.Intent
import android.os.Bundle
import android.telephony.PhoneNumberUtils
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

class InviteFragment : BaseFragment(), KoinComponent {

    private lateinit var inviteViewModel: InviteViewModel
    private lateinit var viewDataBinding: InviteFragmentBinding

    private lateinit var chipsAdapter: ChipsAdapter
    private lateinit var contactAdapter: ContactAdapter

    /**
     * Invoked when user select contact
     * @see [onCreate].
     */
    private val broadcastSelectedAddress: (Contact) -> Unit = {
        val address= it.numbers[0]?.address

        if (address == null) {
            setFailure(Failable.Failure(getString(R.string.fail_cannot_select_address_not_exist), show = true))
        } else {
            context?.sendBroadcast(Intent(ACTION_SET_ADDRESS).putExtra(EXTRA_ADDRESS, address))
        }

        activity?.finish()
    }

    init {
        failables += this
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        inviteViewModel = getViewModel()
        chipsAdapter = ChipsAdapter(context!!)
        contactAdapter = ContactAdapter(onContactClick = broadcastSelectedAddress)

        failables += inviteViewModel.failables
        failables += chipsAdapter
        failables += contactAdapter
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
            editText.setOnTextChanged {
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
}