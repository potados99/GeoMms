package com.potados.geomms.fragment

import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.potados.geomms.R
import com.potados.geomms.adapter.MessageListRecyclerViewAdapter
import com.potados.geomms.data.MessageRepositoryImpl

import com.potados.geomms.dummy.DummyContent
import com.potados.geomms.dummy.DummyContent.DummyItem

/**
 * 메시지 대화 목록을 보여주는 프래그먼트입니다.
 */
class MessageListFragment : Fragment() {

    private lateinit var repo: MessageRepositoryImpl

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
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
            addItemDecoration(
                DividerItemDecoration(
                    activity,
                    DividerItemDecoration.VERTICAL
                )
            )
            layoutManager = LinearLayoutManager(context)
            itemAnimator = DefaultItemAnimator()

            repo = MessageRepositoryImpl(activity!!.contentResolver)
            adapter = MessageListRecyclerViewAdapter(repo.getConversationHeads())
        }

        return view
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)

    }

    override fun onDetach() {
        super.onDetach()
    }

}
