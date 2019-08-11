package com.potados.geomms.feature.compose

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.potados.geomms.common.extension.*
import com.potados.geomms.databinding.ComposeFragmentBinding
import com.potados.geomms.extension.withNonNull
import kotlinx.android.synthetic.main.compose_fragment.messages_recyclerview
import kotlinx.android.synthetic.main.compose_fragment.view.*

class ComposeFragment : Fragment() {


    private lateinit var composeViewModel: ComposeViewModel
    private lateinit var viewDataBinding: ComposeFragmentBinding

    private val adapter = MessagesAdapter()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        composeViewModel = getViewModel { arguments?.getLong(PARAM_CONVERSATION)?.let(::start) }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return ComposeFragmentBinding
            .inflate(inflater, container, false)
            .apply { vm = composeViewModel }
            .apply { viewDataBinding = this }
            .apply { setSupportActionBar(root.toolbar) }
            .apply { initializeView(root) }
            .root
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
            messages_recyclerview.adapter = adapter
        }


        with(view.compose_bottom_layout) {
            addOnLayoutChangeListener { _, _, top, _, bottom, _, _, _, _ ->
                with(view.messages_recyclerview) {
                    setPadding(paddingLeft, paddingTop, paddingRight, bottom - top)

                    if (composeViewModel.recyclerViewReachedItsEnd) {
                        scrollToBottom()
                    }
                }
            }
        }

        with(view.compose_send_button) {
            setOnClickListener {
                //composeViewModel.sendMessage(compose_edittext.text.toString())
                //compose_edittext.text.clear()
            }
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