package com.potados.geomms.fragment

import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.potados.geomms.R
import com.potados.geomms.adapter.MessageListRecyclerViewAdapter
import com.potados.geomms.data.MessageRepository
import com.potados.geomms.data.Sms
import com.potados.geomms.viewmodel.MessageListViewModel
import org.koin.android.ext.android.inject

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

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_message_list, container, false)

        val messageListRecyclerView: RecyclerView = view.findViewById(R.id.fragment_message_list_recyclerview) ?: let {
            Log.e("MessageListFragment:onCreateView()",  "R.id.fragment_message_list_recyclerview is null.")
            return view
        }

        with(messageListRecyclerView) {
            layoutManager = LinearLayoutManager(context)

            viewmodel.getConversationHeads().observe(this@MessageListFragment, object: Observer<List<Sms>> {
                override fun onChanged(t: List<Sms>?) {
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
