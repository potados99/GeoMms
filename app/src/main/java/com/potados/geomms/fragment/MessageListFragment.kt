package com.potados.geomms.fragment

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.*
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.potados.geomms.R
import com.potados.geomms.adapter.MessageListRecyclerViewAdapter
import com.potados.geomms.data.ShortMessage
import com.potados.geomms.viewmodel.MessageListViewModel

/**
 * 메시지 대화 목록을 보여주는 프래그먼트입니다.
 */
class MessageListFragment : Fragment() {

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

        viewmodel.updateConversationHeads()
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

    override fun onAttach(context: Context) {
        super.onAttach(context)
    }

    override fun onDetach() {
        super.onDetach()
    }

    private fun setupAndBindViewWithViewModel(view: View) {
        val toolbar: Toolbar = view.findViewById(R.id.toolbar) ?: let {
            Log.e("MessageListFragment:setupAndBindViewWithViewModel()",  "R.id.toolbar is null.")
            return
        }

        val messageListRecyclerView: RecyclerView = view.findViewById(R.id.fragment_message_list_recyclerview) ?: let {
            Log.e("MessageListFragment:setupAndBindViewWithViewModel()",  "R.id.fragment_message_list_recyclerview is null.")
            return
        }

        setupToolbar(toolbar)

        with(messageListRecyclerView) {
            layoutManager = LinearLayoutManager(context)

            viewmodel.getConversationHeads().observe(this@MessageListFragment, object: Observer<List<ShortMessage>> {
                override fun onChanged(t: List<ShortMessage>?) {
                    t?.also {
                        /**
                         * TODO: RecyclerView.notify****Changed로 변경할 것.
                         */
                        adapter = MessageListRecyclerViewAdapter(it)
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
