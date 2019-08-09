package com.potados.geomms.feature.message

import android.content.IntentFilter
import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.potados.geomms.R
import com.potados.geomms.common.extension.baseActivity
import com.potados.geomms.common.extension.failure
import com.potados.geomms.common.extension.getViewModel
import com.potados.geomms.common.extension.observe
import com.potados.geomms.common.navigation.Navigator
import com.potados.geomms.common.base.NavigationBasedFragment
import com.potados.geomms.app.SmsReceiver
import com.potados.geomms.databinding.ConversationsFragmentBinding
import com.potados.geomms.model.Conversation
import kotlinx.android.synthetic.main.connection_list.view.conversation_list_recyclerview
import org.koin.android.ext.android.inject

/**
 * 메시지 대화 목록을 보여주는 프래그먼트입니다.
 */
class ConversationsFragment : Fragment(),
    ConversationsAdapter.ConversationClickListener {

    private val navigator: Navigator by inject()

    private lateinit var viewModel: ConversationsViewModel
    private lateinit var bindings: ConversationsFragmentBinding

    private val adapter = ConversationsAdapter(this)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewModel = getViewModel {}
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return ConversationsFragmentBinding
            .inflate(inflater, container, false)
            .apply { vm = viewModel }
            .apply { lifecycleOwner = viewLifecycleOwner }
            .apply { bindings = this }
            .root
            .apply { initializeView(this) }
    }

    override fun onConversationClicked(conversation: Conversation) {
        navigator.showComposeActivity(activity!!, conversation)
    }

    private fun initializeView(view: View) {

        /** 대화 목록 리사이클러뷰 */
        with(view.conversation_list_recyclerview) {
            adapter = adapter
            // ...
        }

        // ...
    }
}
