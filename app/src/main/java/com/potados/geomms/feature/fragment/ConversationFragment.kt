package com.potados.geomms.feature.fragment

import android.content.IntentFilter
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.potados.geomms.R
import com.potados.geomms.core.exception.Failure
import com.potados.geomms.core.extension.*
import com.potados.geomms.core.platform.BaseFragment
import com.potados.geomms.core.util.Notify
import com.potados.geomms.feature.adapter.ConversationRecyclerViewAdapter
import com.potados.geomms.feature.data.entity.ShortMessage
import com.potados.geomms.feature.data.entity.SmsThread
import com.potados.geomms.feature.failure.MessageFailure
import com.potados.geomms.feature.receiver.SmsReceiver
import com.potados.geomms.feature.viewmodel.ConversationViewModel
import kotlinx.android.synthetic.main.fragment_conversation.*

class ConversationFragment : BaseFragment() {

    private lateinit var viewModel: ConversationViewModel
    private val adapter = ConversationRecyclerViewAdapter()

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
            observe(messages, ::renderMessages)
            failure(failure, ::handleFailure)

            /** 중요 */
            thread = arguments?.get(PARAM_CONVERSATION) as SmsThread
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


    private fun renderMessages(messages: List<ShortMessage>?) {
        val wasEmpty = adapter.collection.isEmpty()

        adapter.collection = messages.orEmpty()
        scrollToBottom(!wasEmpty)
    }

    private fun handleFailure(failure: Failure?) {
        when(failure) {
            is MessageFailure.QueryFailure -> {
                notifyWithAction(R.string.failure_query, R.string.retry) {
                    viewModel.loadMessages()
                }
            }
            is MessageFailure.SendFailure -> {
                Notify(activity).short("Send failed :(")
            }
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
             * Toolbar 타이틀을 상대방 이름으로 설정.
             */
            conversation_toolbar_title.text = viewModel.recipients()

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
            Log.d("ConversationActivity: scrollToBottom()", "scrolling to bottom.")

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

        fun ofConversation(thread: SmsThread): ConversationFragment=
            ConversationFragment().apply {
                arguments = Bundle().apply {
                    putSerializable(PARAM_CONVERSATION, thread)
                }
            }
    }
}