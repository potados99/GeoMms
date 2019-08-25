package com.potados.geomms.feature.conversations

import android.os.Bundle
import android.view.*
import androidx.appcompat.widget.SearchView
import com.potados.geomms.R
import com.potados.geomms.common.base.NavigationFragment
import com.potados.geomms.common.extension.autoScrollToStart
import com.potados.geomms.common.extension.getViewModel
import com.potados.geomms.common.navigation.Navigator
import com.potados.geomms.databinding.ConversationsFragmentBinding
import com.potados.geomms.manager.PermissionManager
import kotlinx.android.synthetic.main.conversations_fragment.view.*
import kotlinx.android.synthetic.main.main_hint.*
import org.koin.android.ext.android.inject
import timber.log.Timber

/**
 * 메시지 대화 목록을 보여주는 프래그먼트입니다.
 */
class ConversationsFragment : NavigationFragment() {

    override val optionMenuId: Int? = R.menu.conversations
    override val navigationItemId: Int = R.id.menu_item_navigation_message
    override val titleId: Int = R.string.title_conversations

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
            .apply { initializeView(root) }
            .root
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)

        val searchView = menu.findItem(R.id.search).actionView as SearchView

        searchView.setOnQueryTextListener(object: SearchView.OnQueryTextListener {
            // TODO implement
            override fun onQueryTextChange(newText: String?): Boolean {
                return true
            }

            override fun onQueryTextSubmit(query: String?): Boolean {
                return true
            }
        })
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        when (item.itemId) {
            R.id.write -> {
                navigator.showCompose()
            }
        }

        return true // super.onOptionsItemSelected(item)
    }

    override fun onShow() {
        super.onShow()
        Timber.i("ConversationsFragment is shown")
    }
    override fun onHide() {
        super.onHide()
        Timber.i("ConversationsFragment is hidden")
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
