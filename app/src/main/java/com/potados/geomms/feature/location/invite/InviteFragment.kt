package com.potados.geomms.feature.location.invite

import android.content.Intent
import android.os.Bundle
import android.telephony.PhoneNumberUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.potados.geomms.R
import com.potados.geomms.common.base.BaseFragment
import com.potados.geomms.common.extension.getViewModel
import com.potados.geomms.common.extension.isVisible
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

    private val chipsAdapter = ChipsAdapter()
    private val recentAdapter = ContactAdapter()
    private val contactAdapter = ContactAdapter()

    init {
        failables += this
        failables += chipsAdapter
        failables += recentAdapter
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
            .apply { vm = inviteViewModel }
            .apply { lifecycleOwner = this@InviteFragment }
            .apply { viewDataBinding = this }
            .apply { setSupportActionBar(toolbar = root.toolbar, title = false, upButton = true) }
            .apply { initializeView(root) }
            .root
    }

    private fun initializeView(view: View) {

        with(view.chips) {
            adapter = chipsAdapter.apply {
                editText.setOnTextChanged(inviteViewModel::onSearch)
                editText.requestFocus()
            }
        }

        with(view.recents) {
            adapter = recentAdapter.apply {
                companionView = view.recent_group
                onContactClick = { inviteViewModel.onContactClick(activity, it) }
            }
        }

        with(view.contacts) {
            adapter = contactAdapter.apply {
                onContactClick = { inviteViewModel.onContactClick(activity, it) }
            }
        }
    }
}