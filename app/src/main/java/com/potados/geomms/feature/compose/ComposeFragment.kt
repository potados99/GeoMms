package com.potados.geomms.feature.compose

import android.content.res.Resources
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.*
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.potados.geomms.R
import com.potados.geomms.common.base.BaseFragment
import com.potados.geomms.common.extension.*
import com.potados.geomms.databinding.ComposeFragmentBinding
import com.potados.geomms.extension.withNonNull
import kotlinx.android.synthetic.main.compose_fragment.messages_recyclerview
import kotlinx.android.synthetic.main.compose_fragment.view.*
import kotlinx.android.synthetic.main.compose_fragment.view.toolbar
import kotlinx.android.synthetic.main.conversations_fragment.view.*

class ComposeFragment : BaseFragment() {

    override fun optionMenuId(): Int? = R.menu.compose

    private lateinit var composeViewModel: ComposeViewModel
    private lateinit var viewDataBinding: ComposeFragmentBinding

    private lateinit var messagesAdapter: MessagesAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        messagesAdapter = MessagesAdapter(context!!)
        composeViewModel = getViewModel { arguments?.getLong(PARAM_CONVERSATION)?.let(::start) }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return ComposeFragmentBinding
            .inflate(inflater, container, false)
            .apply { vm = composeViewModel }
            .apply { viewDataBinding = this }
            .apply { setSupportActionBar(toolbar = root.toolbar, title = false, upButton = true) }
            .apply { initializeView(root) }
            .root
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId) {
            android.R.id.home -> {
                baseActivity?.finish()
                return true
            }
            else -> {
                // 낫띵..
            }
        }

        return super.onOptionsItemSelected(item)
    }

    private fun initializeView(view: View) {
        with(view.messages_recyclerview) {
            setHasFixedSize(true)
            messagesAdapter.autoScrollToStart(this@with)
            adapter = messagesAdapter
        }

        with(view.send) {
            setOnClickListener {
                with(view.message) {
                    composeViewModel.sendSms(text.toString())
                    text.clear()
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

    companion object {
        private const val PARAM_CONVERSATION = "param_conversation"

        fun ofConversation(conversationId: Long): ComposeFragment =
            ComposeFragment().apply {
                arguments = Bundle().apply {
                    putLong(PARAM_CONVERSATION, conversationId)
                }
            }
    }
}