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

        val messageListRecyclerView: RecyclerView = view.findViewById(R.id.fragment_message_list_recyclerview) ?: let {
            Log.e("MessageListFragment:onCreateView()",  "R.id.fragment_message_list_recyclerview is null.")
            return view
        }

        // TODO: 여기 청소
        val toolbar: Toolbar = view.findViewById(R.id.toolbar)
        (activity as AppCompatActivity).setSupportActionBar(toolbar)
        setHasOptionsMenu(true)


        with(messageListRecyclerView) {
            layoutManager = LinearLayoutManager(context)

            viewmodel.getConversationHeads().observe(this@MessageListFragment, object: Observer<List<ShortMessage>> {
                override fun onChanged(t: List<ShortMessage>?) {
                    t?.also {
                        adapter = MessageListRecyclerViewAdapter(it)
                    }
                }
            })
        }

        return view
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
    }

    override fun onDetach() {
        super.onDetach()
    }

    private fun bindViewWithViewModel(view: View) {

    }

}
