package com.potados.geomms.feature.conversations

import android.os.Bundle
import android.view.*
import androidx.lifecycle.Observer
import com.potados.geomms.R
import com.potados.geomms.common.base.NavigationFragment
import com.potados.geomms.common.extension.*
import com.potados.geomms.common.navigation.Navigator
import com.potados.geomms.databinding.ConversationsFragmentBinding
import com.potados.geomms.manager.PermissionManager
import com.potados.geomms.model.Conversation
import com.potados.geomms.model.SyncLog
import com.potados.geomms.repository.SyncRepository
import com.potados.geomms.usecase.SyncMessages
import io.realm.Realm
import kotlinx.android.synthetic.main.conversations_fragment.view.*
import kotlinx.android.synthetic.main.main_permission_hint.view.*
import org.koin.android.ext.android.inject
import timber.log.Timber

/**
 * 메시지 대화 목록을 보여주는 프래그먼트입니다.
 */
class ConversationsFragment : NavigationFragment(),
    ConversationsAdapter.ConversationClickListener {

    override fun menuId(): Int = R.id.menu_item_navigation_message

    private val navigator: Navigator by inject()
    private val permissionManager: PermissionManager by inject()
    private val syncMessages: SyncMessages by inject()

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
            .apply { viewDataBinding = this }
            .apply { setSupportActionBar(toolbar = root.toolbar, title = false, upButton = false) }
            .apply { initializeView(root) }
            .root
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.compose, menu)
        menu.setTint(context, R.color.primary)

        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onConversationClicked(conversation: Conversation) {
        navigator.showComposeActivity(conversation)
    }


    private fun initializeView(view: View) {

        // If we have all permissions and we've never run a sync, run a sync. This will be the case
        // when upgrading from 2.7.3, or if the app's data was cleared
        val lastSync = Realm.getDefaultInstance().use { realm -> realm.where(SyncLog::class.java)?.max("date") ?: 0 }
        if (lastSync == 0 && permissionManager.isDefaultSms() && permissionManager.hasReadSms() && permissionManager.hasContacts()) {
            syncMessages(Unit)
        }

        permissionManager.isDefaultSmsLiveData().observe(this, object: Observer<Boolean> {
            override fun onChanged(isDefault: Boolean?) {
                isDefault?.let { conversationsViewModel.defaultSmsState = it }
            }
        })

        with(view.snackbar) {
            setOnInflateListener { _, _ ->
                button.setOnClickListener {
                    navigator.showDefaultSmsDialog()
                }
            }
        }

        with(view.conversations) {
            adapter = conversationsAdapter
            setHasFixedSize(true)
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
