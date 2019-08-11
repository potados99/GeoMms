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
import com.potados.geomms.common.extension.*
import com.potados.geomms.databinding.ComposeFragmentBinding
import com.potados.geomms.extension.withNonNull
import kotlinx.android.synthetic.main.compose_fragment.messages_recyclerview
import kotlinx.android.synthetic.main.compose_fragment.view.*
import kotlinx.android.synthetic.main.compose_fragment.view.toolbar
import kotlinx.android.synthetic.main.conversations_fragment.view.*

class ComposeFragment : Fragment() {

    private lateinit var composeViewModel: ComposeViewModel
    private lateinit var viewDataBinding: ComposeFragmentBinding

    private val messagesAdapter = MessagesAdapter()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

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

    override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater?) {
        inflater?.inflate(R.menu.compose, menu)
        menu?.setTint(context, R.color.primary)

        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when(item?.itemId) {
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
        setHasOptionsMenu(true)

        withNonNull(supportActionBar) {
            setDisplayShowTitleEnabled(false)
            setDisplayHomeAsUpEnabled(true)
        }

        with(view.messages_recyclerview) {
            messages_recyclerview.layoutManager = LinearLayoutManager(context)
            messages_recyclerview.adapter = messagesAdapter
        }


        with(view.compose_layout) {
            addOnLayoutChangeListener { _, _, top, _, bottom, _, _, _, _ ->
                with(view.messages_recyclerview) {
                    setPadding(paddingLeft, paddingTop, paddingRight, bottom - top + 30)

                    if (composeViewModel.recyclerViewReachedItsEnd) {
                        scrollToBottom()
                    }
                }
            }
        }

        with(view.send_imageview) {
            setOnClickListener {
                with(view.measage_edittext) {
                    composeViewModel.sendSms(text.toString())
                    text.clear()
                }
            }
        }

        with(view.measage_edittext) {
            view.send_imageview.isEnabled = false
            view.send_imageview.imageAlpha = 128

            addTextChangedListener(object: TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                }
                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                    with(view.send_imageview) {
                        isEnabled = length() > 0
                        imageAlpha = if (length() > 0) 255 else 128
                    }
                }
                override fun afterTextChanged(s: Editable?) {
                }
            })
        }

    }

    private fun scrollToBottom(smooth: Boolean = true) {
        messages_recyclerview.adapter?.let {
            Log.d("ComposeActivity: scrollToBottom()", "scrolling to bottom.")

            val position =  if (it.itemCount > 0) it.itemCount - 1 else 0

            if (smooth) {
                messages_recyclerview.smoothScrollToPosition(position)
            }
            else {
                messages_recyclerview.scrollToPosition(position)
            }

            composeViewModel.recyclerViewReachedItsEnd = true
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