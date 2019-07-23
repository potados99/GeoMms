package com.potados.geomms.feature.fragment

import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.potados.geomms.R
import com.potados.geomms.core.extension.baseActivity
import com.potados.geomms.core.extension.supportActionBar
import com.potados.geomms.core.extension.getViewModel
import com.potados.geomms.core.extension.observe
import com.potados.geomms.core.platform.BaseFragment
import com.potados.geomms.core.util.Notify
import com.potados.geomms.feature.activity.ConversationActivity
import com.potados.geomms.feature.adapter.ConversationRecyclerViewAdapter
import com.potados.geomms.feature.data.entity.ShortMessage
import com.potados.geomms.feature.data.entity.SmsThread
import com.potados.geomms.feature.viewmodel.ConversationViewModel
import kotlinx.android.synthetic.main.fragment_conversation.*

class ConversationFragment : BaseFragment() {

    /**
     * BaseFragment 설정들.
     */
    override fun layoutId(): Int = R.layout.fragment_conversation
    override fun toolbarId(): Int? = R.id.conversation_toolbar
    override fun toolbarMenuId(): Int? = null

    private lateinit var viewModel: ConversationViewModel
    private val adapter = ConversationRecyclerViewAdapter()


    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)

        viewModel = getViewModel {
            observe(getSmsThread(), ::handleSmsThreadChange)
            observe(getMessages(), ::renderMessages)

            val intent = activity?.intent ?: return@getViewModel
            (intent.getSerializableExtra(ConversationActivity.ARG_SMS_THREAD) as SmsThread).let(::setSmsThread)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initializeView(view)
    }

    /**
     * 옵션 버튼이 눌릴 때에 반응합니다.
     */
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

    /**
     * SmsThread가 변경되었을 때에 할 일들.
     */
    private fun handleSmsThreadChange(thread: SmsThread?) {
        conversation_toolbar_title.text = viewModel.getRecipients()
    }

    /**
     * 메시지 목록이 변경되었을 때에 할 일들.
     * (smsThread가 변경되면 이것도 자동으로 변경됨.)
     */
    private fun renderMessages(messages: List<ShortMessage>?) {
        adapter.collection = messages.orEmpty()
    }

    /**
     * 기타 UI 설정 중 동적으로 해야 하는 것들.
     */
    private fun initializeView(view: View) {
        with(view) {

            conversation_recyclerview.layoutManager = LinearLayoutManager(context)
            conversation_recyclerview.adapter = adapter

            supportActionBar?.apply {
                setDisplayShowTitleEnabled(false)
                setDisplayHomeAsUpEnabled(true)
            }

            /**
             * 하단의 메시지 작성 레이아웃이 recyclerView의 컨텐츠를 가리지 않도록
             * 레이아웃 변화에 맞추어 recyclerView의 패딩을 설정해줍니다.
             */
            conversation_bottom_layout.addOnLayoutChangeListener { _, _, top, _, bottom, _, _, _, _ ->
                conversation_recyclerview.apply {
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
        }
    }

    /**
     * 메시지 목록 recyclerView를 최하단으로 스크롤합니다.
     */
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
}