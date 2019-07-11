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
import com.potados.geomms.adapter.ConversationListRecyclerViewAdapter
import com.potados.geomms.data.SmsThread
import com.potados.geomms.util.SerializableContainer
import com.potados.geomms.viewmodel.ConversationListViewModel
import kotlinx.android.synthetic.main.fragment_conversation_list.view.*

/**
 * 메시지 대화 목록을 보여주는 프래그먼트입니다.
 */
class ConversationListFragment : Fragment(), ConversationListRecyclerViewAdapter.ConversationClickListener {

    /**
     * 데이터와 마짱을 뜨려면 이친구만 있으면 됩니다. ㅎㅎ
     */
    private lateinit var viewmodel: ConversationListViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewmodel = ViewModelProviders.of(this).get(ConversationListViewModel::class.java)
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
        val view = inflater.inflate(R.layout.fragment_conversation_list, container, false)

        setupAndBindViewWithViewModel(view)

        return view
    }

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


    override fun onAttach(context: Context) {
        super.onAttach(context)
    }

    override fun onDetach() {
        super.onDetach()
    }



    private fun setupAndBindViewWithViewModel(view: View) {
        val toolbar: Toolbar = view.conversation_list_toolbar
        val messageListRecyclerView: RecyclerView = view.conversation_list_recyclerview

        setupToolbar(toolbar)

        with(messageListRecyclerView) {
            layoutManager = LinearLayoutManager(context)

            viewmodel.getConversations().observe(this@ConversationListFragment, object: Observer<List<SmsThread>> {
                override fun onChanged(t: List<SmsThread>?) {
                    if (t == null) return

                    /**
                     * TODO: RecyclerView.notify****Changed로 변경할 것.
                     */
                    adapter = ConversationListRecyclerViewAdapter(t, this@ConversationListFragment)
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
