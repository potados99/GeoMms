package com.potados.geomms.feature.conversations

import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import com.potados.geomms.common.extension.getViewModel
import com.potados.geomms.common.navigation.Navigator
import com.potados.geomms.databinding.ConversationsFragmentBinding
import com.potados.geomms.model.Conversation
import kotlinx.android.synthetic.main.conversations_fragment.view.*
import org.koin.android.ext.android.inject

/**
 * 메시지 대화 목록을 보여주는 프래그먼트입니다.
 */
class ConversationsFragment : Fragment(),
    ConversationsAdapter.ConversationClickListener {

    private val navigator: Navigator by inject()

    private lateinit var conversationsViewModel: ConversationsViewModel
    private lateinit var viewDataBinding: ConversationsFragmentBinding

    private val conversationsAdapter = ConversationsAdapter(this)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        conversationsViewModel = getViewModel {}
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return ConversationsFragmentBinding
            .inflate(inflater, container, false)
            .apply { vm = conversationsViewModel }
            .apply { viewDataBinding = this }
            .root
            .apply { initializeView(this) }
    }

    override fun onConversationClicked(conversation: Conversation) {
        navigator.showComposeActivity(activity!!, conversation)
    }

    private fun initializeView(view: View) {

        /** 대화 목록 리사이클러뷰 */
        with(view.conversations_recyclerview) {
            adapter = conversationsAdapter
            // ...
        }

        // ...
    }
}
