package com.potados.geomms.feature.fragment

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.view.*
import androidx.recyclerview.widget.LinearLayoutManager
import com.potados.geomms.R
import com.potados.geomms.core.exception.Failure
import com.potados.geomms.core.extension.baseActivity
import com.potados.geomms.core.extension.failure
import com.potados.geomms.core.extension.getViewModel
import com.potados.geomms.core.extension.observe
import com.potados.geomms.core.navigation.Navigator
import com.potados.geomms.core.platform.NavigationBasedFragment
import com.potados.geomms.core.util.Notify
import com.potados.geomms.feature.adapter.ConversationListRecyclerViewAdapter
import com.potados.geomms.feature.data.entity.SmsThread
import com.potados.geomms.feature.failure.MessageFailure
import com.potados.geomms.feature.receiver.SmsReceiver
import com.potados.geomms.feature.viewmodel.ConversationListViewModel
import kotlinx.android.synthetic.main.fragment_conversation_list.view.conversation_list_recyclerview
import org.koin.android.ext.android.inject

/**
 * 메시지 대화 목록을 보여주는 프래그먼트입니다.
 */
class ConversationListFragment : NavigationBasedFragment(),
    ConversationListRecyclerViewAdapter.ConversationClickListener {

    private val navigator: Navigator by inject()

    private lateinit var viewModel: ConversationListViewModel
    private val adapter = ConversationListRecyclerViewAdapter(this)

    /**
     * NavigationBasedFragment 설정들.
     */
    override fun layoutId(): Int = R.layout.fragment_conversation_list
    override fun toolbarId(): Int? = R.id.conversation_list_toolbar
    override fun toolbarMenuId(): Int? = R.menu.toolbar_menu
    override fun menuItemId(): Int = R.id.menu_item_navigation_message
    override fun smsReceivedBehavior() = { _: String, _: String, _: Long ->
        viewModel.loadConversations()
    }
    override fun intentFilter(): IntentFilter? = IntentFilter(SmsReceiver.SMS_DELIVER_ACTION)


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewModel = getViewModel {
            observe(conversations, ::renderConversations)
            failure(failure, ::handleFailure)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initializeView(view)
        viewModel.loadConversations()
    }

    override fun onConversationClicked(conversation: SmsThread) {
        viewModel.setAsRead(conversation)
        viewModel.loadConversations()
        navigator.showConversationActivity(baseActivity, conversation)
    }

    private fun renderConversations(threadList: List<SmsThread>?) {
            adapter.collection = threadList.orEmpty()
    }

    private fun initializeView(view: View) {
        view.conversation_list_recyclerview.layoutManager = LinearLayoutManager(context)
        view.conversation_list_recyclerview.adapter = adapter
    }

    private fun handleFailure(failure: Failure?) {
        when(failure) {
            is MessageFailure.QueryFailure -> {
                notifyWithAction(R.string.failure_query, R.string.retry) {
                    viewModel.loadConversations()
                }
            }
            else -> {
                Notify(activity).short("what??")
            }
        }
    }
}
