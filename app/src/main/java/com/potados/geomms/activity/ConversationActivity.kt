package com.potados.geomms.activity

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.potados.geomms.R
import com.potados.geomms.adapter.ConversationRecyclerViewAdapter
import com.potados.geomms.data.ShortMessage
import com.potados.geomms.data.SmsThread
import com.potados.geomms.util.Blur
import com.potados.geomms.viewmodel.ConversationViewModel
import kotlinx.android.synthetic.main.activity_conversation.*

/**
 * 대화방 액티비티입니다.
 */
class ConversationActivity : AppCompatActivity() {

    /**
     * 뷰모델입니다.
     */
    private lateinit var viewModel: ConversationViewModel

    /**
     * 시작점입니다.
     * 하나만 instantiate 하도록 보장하고,
     * 뷰모델을 가져온 뒤 나머지 UI를 설정합니다.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        if (isInstantiated()) finish()
        instantiated = true

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_conversation)

        viewModel = ViewModelProviders.of(this).get(ConversationViewModel::class.java)
        bindUi()

        setUpUi()
    }

    /**
     * 객체가 소멸될 때에 이를 알립니다.
     */
    override fun onDestroy() {
        super.onDestroy()
        instantiated = false
    }

    /**
     * 옵션 메뉴를 달아줍니다.
     */
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        return super.onCreateOptionsMenu(menu)
    }

    /**
     * 옵션 버튼이 눌릴 때에 반응합니다.
     */
    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when(item?.itemId) {
            android.R.id.home -> {
                this.finish()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    /**
     * UI를 뷰모델과 이어줍니다.
     */
    private fun bindUi() {

        /**
         * smsThread가 변경되었을 때에 할 일들
         */
        viewModel.getSmsThread().observe(this, object: Observer<SmsThread> {
            override fun onChanged(t: SmsThread?) {
                if (t == null) return

                /**
                 * 툴바 타이틀 설정.
                 */
                conversation_toolbar_title.text = viewModel.getRecipients()

                /**
                 * 메시지 목록을 여기서 변경하지는 않는다.
                 * 뷰모델에 변화를 가하는 행동은 여기에 기술되면 안된다.
                 * 대신 뷰모델의 setSmsThread()에 배치한다.
                 */
            }
        })

        /**
         * 메시지 목록이 변경되었을 때에 할 일들.
         * (smsThread가 변경되면 이것도 자동으로 변경됨.)
         */
        viewModel.getMessages().observe(this, object: Observer<List<ShortMessage>> {
            override fun onChanged(t: List<ShortMessage>?) {
                if (t == null) return

                conversation_recyclerview.adapter = ConversationRecyclerViewAdapter(t)

                /**
                 * 항상 최하단으로 스크롤할 필요는 없다.
                 * TODO: 조건 부여하기.
                 */
                scrollToBottom(false)
            }
        })

        /**
         * ConversationListFragment에서 집어넣은 SmsThread 꺼냅니다.
         */
        val smsThread = intent.getSerializableExtra(ARG_SMS_THREAD) as SmsThread

        /**
         * 가즈아
         */
        viewModel.setSmsThread(smsThread)
    }

    /**
     * 기타 UI 설정 중 동적으로 해야 하는 것들.
     */
    private fun setUpUi() {
        /**
         * 리사이클러뷰 외관 설정하기.
         */
        conversation_recyclerview.layoutManager = LinearLayoutManager(this@ConversationActivity)

        /**
         * 액션바 설정하기.
         */
        setSupportActionBar(conversation_toolbar)
        supportActionBar?.apply {
            setDisplayShowTitleEnabled(false)
            setDisplayHomeAsUpEnabled(true)
        }

        /**
         * 메시지 작성하는 editText 레이아웃 배경 블러 채우기.
         */
        // val drawable = ColorDrawable(Color.parseColor("#FAFAFA"))
        // conversation_bottom_layout.background = Blur.applyBlur(drawable, this).apply { alpha = 200 }

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
        conversation_recyclerview.addOnScrollListener(object: RecyclerView.OnScrollListener() {
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

    /**
     * 메시지 목록 recyclerView를 최하단으로 스크롤합니다.
     */
    private fun scrollToBottom(smooth: Boolean = true) {
        conversation_recyclerview.adapter?.let {
            Log.d("ConversationActivity: scrollToBottom()", "scrolling to bottom.")

            if (smooth) {
                conversation_recyclerview.smoothScrollToPosition(it.itemCount - 1)
            }
            else {
                conversation_recyclerview.scrollToPosition(it.itemCount - 1)
            }

            viewModel.recyclerViewReachedItsEnd = true
        }
    }

    companion object {
        const val ARG_SMS_THREAD = "arg_sms_thread"

        /**
         * 나만 건드릴 수 있지
         */
        private var instantiated = false
        fun isInstantiated() = instantiated
    }
}
