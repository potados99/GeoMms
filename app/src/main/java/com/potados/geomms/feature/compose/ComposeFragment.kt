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
import kotlinx.android.synthetic.main.compose_fragment.*

class ComposeFragment : Fragment() {


    private lateinit var viewModel: ComposeViewModel
    private lateinit var viewDataBinding: ComposeFragmentBinding

    private val adapter = MessagesAdapter()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewModel = getViewModel {
            /** 중요 */
           arguments?.getLong(PARAM_CONVERSATION)?.let(::start)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return ComposeFragmentBinding
            .inflate(inflater, container, false)
            .apply { vm = viewModel }
            .apply { viewDataBinding = this }
            .root
            .apply { initializeView(this) }
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when(item?.itemId) {
            android.R.id.home -> {
                activity!!.finish()
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

        with(view) {

            /**
             * 리사이클러뷰 설정.
             */
            messages_recyclerview.layoutManager = LinearLayoutManager(context)
            messages_recyclerview.adapter = adapter

            /**
             * 툴바 설정.
             */
            (activity as AppCompatActivity).supportActionBar?.apply {
                setDisplayShowTitleEnabled(false)
                setDisplayHomeAsUpEnabled(true)
            }

            /**
             * 하단의 메시지 작성 레이아웃이 recyclerView의 컨텐츠를 가리지 않도록
             * 레이아웃 변화에 맞추어 recyclerView의 패딩을 설정해줍니다.
             */
            compose_bottom_layout.addOnLayoutChangeListener { _, _, top, _, bottom, _, _, _, _ ->
                with(messages_recyclerview) {
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
            messages_recyclerview.addOnScrollListener(object : RecyclerView.OnScrollListener() {
                override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                    super.onScrollStateChanged(recyclerView, newState)

                    /**
                     * direction -1은 위, 1은 아래입니다.
                     * 아래로 스크롤할 수 없다면 바닥에 도달한 것입니다.
                     */
                    viewModel.recyclerViewReachedItsEnd = !messages_recyclerview.canScrollVertically(1)
                }
            })

            /**
             * 보내기 버튼 동작 설정.
             */
            compose_send_button.setOnClickListener {
                //viewModel.sendMessage(compose_edittext.text.toString())
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