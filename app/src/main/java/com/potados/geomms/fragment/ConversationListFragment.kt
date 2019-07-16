package com.potados.geomms.fragment

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.telephony.SmsMessage
import android.util.Log
import android.view.*
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.potados.geomms.R
import com.potados.geomms.activity.ConversationActivity
import com.potados.geomms.activity.MainActivity
import com.potados.geomms.adapter.ConversationListRecyclerViewAdapter
import com.potados.geomms.data.entity.SmsThread
import com.potados.geomms.receiver.SmsReceiver
import com.potados.geomms.util.Notify
import com.potados.geomms.viewmodel.ConversationListViewModel
import kotlinx.android.synthetic.main.fragment_conversation_list.view.*
import kotlinx.android.synthetic.main.fragment_conversation_list.view.conversation_list_recyclerview
import java.util.*

/**
 * 메시지 대화 목록을 보여주는 프래그먼트입니다.
 */
class ConversationListFragment : Fragment(), ConversationListRecyclerViewAdapter.ConversationClickListener {

    /**
     * 뷰모델입니다.
     */
    private lateinit var viewModel: ConversationListViewModel

    private val receiver = object: BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            viewModel.updateConversations()
        }
    }
    private val filter = IntentFilter("com.potados.geomms.SMS_DELIVER")


    /**
     * 프래그먼트가 액티비티에 붙을 때에 실행됩니다.
     */
    override fun onAttach(context: Context) {
        super.onAttach(context)
        Log.i("ConversationListFragment: onAttach", "attached.")
    }

    /**
     * 제일 먼저 실행됩니다.
     * 뷰모델을 가져옵니다.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        /**
         * 뷰모델 가져오기
         */
        viewModel = ViewModelProviders
            .of(activity ?: throw RuntimeException("activity is null."))
            .get(ConversationListViewModel::class.java)

        Log.i("ConversationListFragment: onCreate", "created.")
    }
    /**
     * 뷰가 만들어질 때에 실행됩니다.
     * 뷰를 찾아서 뷰모델과 연결해줍니다.
     * 뷰 설정도 해줍니다. (programmatically)
     */
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_conversation_list, container, false).also {
            bindUi(it)
            setUpUi(it)

            Log.i("ConversationListFragment: onCreateView", "view created.")
        }
    }

    override fun onStart() {
        super.onStart()

        context?.registerReceiver(receiver, filter)
    }

    /**
     * 재개될 때 실행됩니다.
     */
    override fun onResume() {
        super.onResume()

        Log.i("ConversationListFragment: onResume", "resumed.")
    }

    /**
     * 멈출 때 실행됩니다.
     */
    override fun onPause() {
        super.onPause()

        Log.i("ConversationListFragment: onPause", "paused.")
    }

    override fun onStop() {
        super.onStop()

        context?.unregisterReceiver(receiver)
    }

    /**
     * 생명주기 종료입니다.
     */
    override fun onDestroy() {
        super.onDestroy()
        Log.i("ConversationListFragment: onDestroy", "destroyed.")
    }

    /**
     * 프래그먼트가 액티비티에서 떨어질 때에 실행됩니다.
     */
    override fun onDetach() {
        super.onDetach()
        Log.i("ConversationListFragment: onDetach", "detached.")
    }

    /**
     * 옵션 메뉴를 만들어줍니다.
     */
    override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater?) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater?.inflate(R.menu.toobar_menu, menu)
    }

    /**
     * 대화방을 눌렀을 때에 반응합니다.
     */
    override fun onConversationClicked(conversation: SmsThread) {
        startActivity(Intent(context, ConversationActivity::class.java).apply {
            /**
             * 직접 객체를 집어넣습니다.
             * id만 넘긴 후 다시 query하는것보다 이게 낫다고 판단.
             * SmsThread는 Serializable 합니다.
             */
            putExtra(ConversationActivity.ARG_SMS_THREAD, conversation)
        })
    }

    /**
     * UI를 뷰모델과 이어줍니다.
     */
    private fun bindUi(view: View) {

        /**
         * 대화 목록이 바뀔 때에 해야 할 일들.
         */
        viewModel.getConversations().observe(this, object: Observer<List<SmsThread>> {
            override fun onChanged(t: List<SmsThread>?) {
                if (t == null) return

                view.conversation_list_recyclerview.adapter = ConversationListRecyclerViewAdapter(
                    t, this@ConversationListFragment
                )
            }
        })
    }

    /**
     * 동적 UI 설정.
     */
    private fun setUpUi(view: View) {

        /**
         * 리사이클러뷰 외관 설정
         */
        view.conversation_list_recyclerview.layoutManager = LinearLayoutManager(context)

        /**
         * 액션바 설정하기.
         *
         * 시스템의 기본 AppBar를 사용하지 않고 (manifest에서 NoActionBar 사용)
         * 프래그먼트 내의 view에서 Toolbar를 사용했기 때문에,
         * 이 Toolbar를 supportActionBar로 등록.
         */
        (activity as AppCompatActivity).setSupportActionBar(view.conversation_list_toolbar)
        setHasOptionsMenu(true)
    }
}
