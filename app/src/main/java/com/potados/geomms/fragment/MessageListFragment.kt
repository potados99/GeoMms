package com.potados.geomms.fragment

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.*
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.potados.geomms.R
import com.potados.geomms.activity.ConversationActivity
import com.potados.geomms.adapter.MessageListRecyclerViewAdapter
import com.potados.geomms.data.ShortMessage
import com.potados.geomms.data.SmsThread
import com.potados.geomms.util.ContactHelper
import com.potados.geomms.util.Notify
import com.potados.geomms.viewmodel.MessageListViewModel
import kotlinx.android.synthetic.main.fragment_message_list.view.*

/**
 * 메시지 대화 목록을 보여주는 프래그먼트입니다.
 */
class MessageListFragment : Fragment(), MessageListRecyclerViewAdapter.ConversationClickListener {

    /**
     * 데이터와 마짱을 뜨려면 이친구만 있으면 됩니다. ㅎㅎ
     */
    private lateinit var viewmodel: MessageListViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewmodel = ViewModelProviders.of(this).get(MessageListViewModel::class.java)
    }

    override fun onResume() {
        super.onResume()

        viewmodel.updateConversations()
    }

    override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater?) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater?.inflate(R.menu.toobar_menu, menu)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_message_list, container, false)

        setupAndBindViewWithViewModel(view)

        return view
    }

    override fun onConversationClicked(conversationHead: SmsThread) {
        startActivity(Intent(context, ConversationActivity::class.java).apply {
            putExtra(ConversationActivity.ARG_ADDRESS, ContactHelper.getPhoneNumberByRecipientId(this@MessageListFragment.context!!.contentResolver, conversationHead.getRecipientIds()[0]))
            putExtra(ConversationActivity.ARG_THREAD_ID, conversationHead.id)
        })
    }


    override fun onAttach(context: Context) {
        super.onAttach(context)
    }

    override fun onDetach() {
        super.onDetach()
    }



    private fun setupAndBindViewWithViewModel(view: View) {
        val toolbar: Toolbar = view.toolbar
        val messageListRecyclerView: RecyclerView = view.fragment_message_list_recyclerview

        setupToolbar(toolbar)

        with(messageListRecyclerView) {
            layoutManager = LinearLayoutManager(context)

            viewmodel.getConversations().observe(this@MessageListFragment, object: Observer<List<SmsThread>> {
                override fun onChanged(t: List<SmsThread>?) {
                    t?.also {
                        /**
                         * TODO: RecyclerView.notify****Changed로 변경할 것.
                         */
                        adapter = MessageListRecyclerViewAdapter(t, this@MessageListFragment)
                    }
                }
            })
        }
    }

    /**
     * 시스템의 기본 AppBar를 사용하지 않고 (manifest에서 NoActionBar 사용)
     * 프래그먼트 내의 view에서 Toolbar를 사용했기 때문에, 이 Toolbar를 supportActionBar로 등록해주어야 합니다.
     */
    private fun setupToolbar(bar: Toolbar) {
        (activity as AppCompatActivity).setSupportActionBar(bar)
        setHasOptionsMenu(true)
    }

}
