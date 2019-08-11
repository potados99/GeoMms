package com.potados.geomms.feature.conversations

import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import com.potados.geomms.R
import com.potados.geomms.common.base.NavigationFragment
import com.potados.geomms.common.extension.*
import com.potados.geomms.common.navigation.Navigator
import com.potados.geomms.databinding.ConversationsFragmentBinding
import com.potados.geomms.extension.withNonNull
import com.potados.geomms.model.Conversation
import com.potados.geomms.repository.SyncRepository
import com.potados.geomms.usecase.SyncMessages
import kotlinx.android.synthetic.main.conversations_fragment.view.*
import org.koin.android.ext.android.inject
import timber.log.Timber

/**
 * 메시지 대화 목록을 보여주는 프래그먼트입니다.
 */
class ConversationsFragment : NavigationFragment(),
    ConversationsAdapter.ConversationClickListener {

    override fun menuId(): Int = R.id.menu_item_navigation_message

    private val navigator: Navigator by inject()

    private lateinit var conversationsViewModel: ConversationsViewModel
    private lateinit var viewDataBinding: ConversationsFragmentBinding

    private val conversationsAdapter = ConversationsAdapter(this)

    // TODO remove
    private val syncMessages: SyncMessages by inject()
    private val syncRepo: SyncRepository by inject()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        conversationsViewModel = getViewModel()

        // TODO
        syncMessages(Unit) {
            it.onError {
                Timber.w(it)
            }
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return ConversationsFragmentBinding
            .inflate(inflater, container, false)
            .apply { vm = conversationsViewModel }
            .apply { viewDataBinding = this }
            .apply { setSupportActionBar(toolbar = root.toolbar, title = false, upButton = false) }
            .apply { initializeView(root) }
            .root
    }

    override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater?) {
        inflater?.inflate(R.menu.compose, menu)
        menu?.setTint(context, R.color.primary)

        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onResume() {
        super.onResume()
        //syncRepo.syncMessages()
        /*syncMessages(Unit) {
            it.onError {
                Timber.w(it)
            }
        }*/
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

        with(view.toolbar_search) {
            setVisible(true)
        }

        with(view.toolbar_title) {
            setVisible(false)
        }
    }
}
