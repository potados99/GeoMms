package com.potados.geomms.feature.message

import android.content.IntentFilter
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.potados.geomms.R
import com.potados.geomms.common.extension.*
import com.potados.geomms.common.base.BaseFragment
import com.potados.geomms.app.SmsReceiver
import com.potados.geomms.feature.message.domain.Conversation
import com.potados.geomms.feature.message.domain.Sms
import kotlinx.android.synthetic.main.conversations_fragment.*

class ComposeFragment : BaseFragment() {

    private lateinit var viewModel: ComposeViewModel
    private val adapter = ComposeAdapter()

    /**
     * BaseFragment 설정들.
     */
    override fun layoutId(): Int = R.layout.fragment_conversation
    override fun toolbarId(): Int? = R.id.conversation_toolbar
    override fun toolbarMenuId(): Int? = null
    override fun smsReceivedBehavior() = { _: String, _: String, _: Long ->
        viewModel.loadMessages()
        viewModel.setAsRead()
    }

    override fun intentFilter(): IntentFilter? = IntentFilter(SmsReceiver.SMS_DELIVER_ACTION)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewModel = getViewModel {
            observe(conversation, ::renderConversation)
            observe(messages, ::renderMessages)

            failure(failure, ::handleFailure)

            /** 중요 */
           arguments?.getLong(PARAM_CONVERSATION)?.let(::start)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initializeView(view)
        viewModel.setAsRead()
    }

    override fun onResume() {
        super.onResume()

        viewModel.loadMessages()
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when(item?.itemId) {
            android.R.id.home -> {
                baseActivity.finish()
                return true
            }
            else -> {
                // 낫띵..
            }
        }

        return super.onOptionsItemSelected(item)
    }

    private fun renderConversation(conversation: Conversation?) {
        conversation?.let {
            conversation_toolbar_title.text = it.recipients.map { it.contactName ?: it.phoneNumber }.serialize()
        }
    }

    private fun renderMessages(messages: List<Sms>?) {
        val wasEmpty = adapter.collection.isEmpty()

        adapter.collection = messages.orEmpty()
        scrollToBottom(!wasEmpty)
    }

    private fun handleFailure(failure: Exception?) {
        failure?.let {
            notify(it.message ?: it::class.java.name)
            it.printStackTrace()
        }
    }

    private fun initializeView(view: View) {
        with(view) {

            /**
             * 리사이클러뷰 설정.
             */
            conversation_recyclerview.layoutManager = LinearLayoutManager(context)
            conversation_recyclerview.adapter = adapter

            /**
             * 툴바 설정.
             */
            supportActionBar?.apply {
                setDisplayShowTitleEnabled(false)
                setDisplayHomeAsUpEnabled(true)
            }

            /**
             * 하단의 메시지 작성 레이아웃이 recyclerView의 컨텐츠를 가리지 않도록
             * 레이아웃 변화에 맞추어 recyclerView의 패딩을 설정해줍니다.
             */
            conversation_bottom_layout.addOnLayoutChangeListener { _, _, top, _, bottom, _, _, _, _ ->
                with(conversation_recyclerview) {
                    setPadding(paddingLeft, paddingTop, paddingRight, bottom - top)

                    if (viewModel.recyclerViewReachedItsEnd) {
                        scrollToBottom()
                    }
                }
            }

            /**
             * 메시지 목록의 스크롤 상태에 따라 특정 상황에서 맨 밑으로 스크롤할지 여부가 달라집니다.
             * 스크롤 상태가 바뀔때마다 현재 바닥에 도달했는지 여부를 저장합니다.
             */
            conversation_recyclerview.addOnScrollListener(object : RecyclerView.OnScrollListener() {
                override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                    super.onScrollStateChanged(recyclerView, newState)

                    /**
                     * direction -1은 위, 1은 아래입니다.
                     * 아래로 스크롤할 수 없다면 바닥에 도달한 것입니다.
                     */
                    viewModel.recyclerViewReachedItsEnd = !conversation_recyclerview.canScrollVertically(1)
                }
            })

            /**
             * 보내기 버튼 동작 설정.
             */
            conversation_send_button.setOnClickListener {
                viewModel.sendMessage(conversation_edittext.text.toString())
                conversation_edittext.text.clear()
            }
        }
    }

    private fun scrollToBottom(smooth: Boolean = true) {
        conversation_recyclerview.adapter?.let {
            Log.d("ComposeActivity: scrollToBottom()", "scrolling to bottom.")

            val position =  if (it.itemCount > 0) it.itemCount - 1 else 0

            if (smooth) {
                conversation_recyclerview.smoothScrollToPosition(position)
            }
            else {
                conversation_recyclerview.scrollToPosition(position)
            }

            viewModel.recyclerViewReachedItsEnd = true
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