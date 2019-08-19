package com.potados.geomms.feature.conversations

import android.os.Bundle
import android.view.*
import androidx.lifecycle.Observer
import com.potados.geomms.R
import com.potados.geomms.common.base.NavigationFragment
import com.potados.geomms.common.extension.*
import com.potados.geomms.common.navigation.Navigator
import com.potados.geomms.databinding.ConversationsFragmentBinding
import com.potados.geomms.extension.withNonNull
import com.potados.geomms.manager.PermissionManager
import com.potados.geomms.model.Conversation
import com.potados.geomms.model.SyncLog
import com.potados.geomms.repository.SyncRepository
import com.potados.geomms.usecase.SyncMessages
import io.realm.Realm
import kotlinx.android.synthetic.main.conversations_fragment.view.*
import kotlinx.android.synthetic.main.main_permission_hint.view.*
import org.koin.android.ext.android.inject
import org.koin.core.inject
import timber.log.Timber

/**
 * 메시지 대화 목록을 보여주는 프래그먼트입니다.
 */
class ConversationsFragment : NavigationFragment() {

    override fun optionMenuId(): Int? = R.menu.conversations
    override fun navigationItemId(): Int = R.id.menu_item_navigation_message

    private val navigator: Navigator by inject()
    private val permissionManager: PermissionManager by inject()

    private lateinit var conversationsViewModel: ConversationsViewModel
    private lateinit var viewDataBinding: ConversationsFragmentBinding

    private val conversationsAdapter = ConversationsAdapter()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        conversationsViewModel = getViewModel()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return ConversationsFragmentBinding
            .inflate(inflater, container, false)
            .apply { vm = conversationsViewModel }
            .apply { lifecycleOwner = this@ConversationsFragment }
            .apply { viewDataBinding = this }
            .apply { setSupportActionBar(toolbar = root.toolbar, title = false, upButton = false) }
            .apply { initializeView(root) }
            .root
    }

    override fun onResume() {
        super.onResume()
        permissionManager.refresh()
    }

    private fun initializeView(view: View) {

        with(view.snackbar) {
            button.setOnClickListener {
                navigator.showDefaultSmsDialog()
            }
        }

        with(view.conversations) {
            setHasFixedSize(true)
            conversationsAdapter.autoScrollToStart(this@with)
            adapter = conversationsAdapter
        }
    }
}
