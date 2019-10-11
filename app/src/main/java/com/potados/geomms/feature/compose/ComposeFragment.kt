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

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.*
import com.potados.geomms.R
import com.potados.geomms.common.base.BaseFragment
import com.potados.geomms.common.extension.*
import com.potados.geomms.databinding.ComposeFragmentBinding
import com.potados.geomms.util.Notify
import kotlinx.android.synthetic.main.compose_fragment.view.*
import timber.log.Timber

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
                composeViewModel.call(activity)
            }
            R.id.location -> {
                composeViewModel.showOnMap(activity)
            }
            R.id.info -> {
                Notify(context).short(R.string.notify_not_implemented)
            }
        }

        return true
    }

    override fun onResume() {
        super.onResume()
        composeViewModel.setActiveConversation()
    }

    override fun onPause() {
        super.onPause()
        composeViewModel.unsetActiveConversation()
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
                    contactAdapter.data = composeViewModel.getSearchResult(it)
                }

                editText.requestFocus()
            }
        }

        with(view.contacts) {
            adapter = contactAdapter.apply {
                onContactClick = { composeViewModel.setConversationByContact(it) }

                data = composeViewModel.getContacts()
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
}