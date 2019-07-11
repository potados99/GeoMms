package com.potados.geomms.activity

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import com.potados.geomms.R
import com.potados.geomms.adapter.ConversationRecyclerViewAdapter
import com.potados.geomms.data.ShortMessage
import com.potados.geomms.data.SmsThread
import com.potados.geomms.util.SerializableContainer
import com.potados.geomms.viewmodel.ConversationViewModel
import kotlinx.android.synthetic.main.activity_conversation.*

/**
 * 대화방 액티비티입니다.
 */
class ConversationActivity : AppCompatActivity() {

    private lateinit var viewModel: ConversationViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        if (isInstantiated()) finish()
        instantiated = true

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_conversation)

        setUpViewModelAndUi()
    }

    override fun onDestroy() {
        super.onDestroy()
        instantiated = false
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when(item?.itemId) {
            android.R.id.home -> {
                this.finish()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun setUpViewModelAndUi() {
        /**
         * 뷰모델 가져오기
         */
        viewModel = ViewModelProviders.of(this).get(ConversationViewModel::class.java)

        /**
         * smsThread가 변경되었을 때에 할 일들
         */
        viewModel.getSmsThread().observe(this, object: Observer<SmsThread> {
            override fun onChanged(t: SmsThread?) {
                if (t == null) return

                setToolbarTitle(viewModel.getRecipients())

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

                activity_conversation_recyclerview.adapter = ConversationRecyclerViewAdapter(t)

                /**
                 * 항상 최하단으로 스크롤할 필요는 없다.
                 * TODO: 조건 부여하기.
                 */
                activity_conversation_recyclerview.scrollToPosition(t.size - 1)
            }
        })

        /**
         * 리사이클러뷰 외관 설정하기
         */
        activity_conversation_recyclerview.layoutManager = LinearLayoutManager(this@ConversationActivity)

        /**
         * 액션바 설정하기
         */
        setSupportActionBar(activity_conversation_toolbar)
        supportActionBar?.apply {
            setDisplayShowTitleEnabled(false)
            setDisplayHomeAsUpEnabled(true)
        }

        /**
         * ConversationListFragment에서 집어넣은 SmsThread 꺼냅니다.
         * 가즈아
         */
        val smsThread = intent.getSerializableExtra(ARG_SMS_THREAD) as SmsThread
        viewModel.setSmsThread(smsThread)
    }


    private fun setToolbarTitle(title: String) {
        activity_conversation_toolbar_title.text = title
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
