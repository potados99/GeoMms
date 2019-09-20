package com.potados.geomms.feature.conversations

import android.os.Bundle
import android.view.*
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.FragmentActivity
import com.potados.geomms.R
import com.potados.geomms.common.base.NavigationFragment
import com.potados.geomms.common.extension.autoScrollToStart
import com.potados.geomms.common.extension.getViewModel
import com.potados.geomms.common.extension.observe
import com.potados.geomms.common.navigation.Navigator
import com.potados.geomms.databinding.ConversationsFragmentBinding
import com.potados.geomms.manager.PermissionManager
import com.potados.geomms.model.Conversation
import com.potados.geomms.model.SearchResult
import com.potados.geomms.usecase.DeleteConversations
import com.potados.geomms.util.Popup
import kotlinx.android.synthetic.main.conversations_fragment.view.*
import kotlinx.android.synthetic.main.main_hint.view.*
import org.koin.core.inject
import timber.log.Timber

class ConversationsFragment : NavigationFragment() {

    override val optionMenuId: Int? = R.menu.conversations
    override val navigationItemId: Int = R.id.menu_item_navigation_message
    override val titleId: Int = R.string.title_conversations

    private lateinit var conversationsViewModel: ConversationsViewModel
    private lateinit var viewDataBinding: ConversationsFragmentBinding

    private val conversationsAdapter = ConversationsAdapter()
    private val searchAdapter = SearchAdapter()

    init {
        failables += this
        failables += conversationsAdapter
        failables += searchAdapter
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        conversationsViewModel = getViewModel()
        failables += conversationsViewModel.failables
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

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        // Start after sheetView initiated.
        conversationsViewModel.start()
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)

        val searchView = menu.findItem(R.id.search).actionView as SearchView

        searchView.setOnQueryTextListener(object: SearchView.OnQueryTextListener {
            override fun onQueryTextChange(newText: String?): Boolean {
                conversationsViewModel.typeQuery(newText)
                return true
            }
            override fun onQueryTextSubmit(query: String?): Boolean {
                conversationsViewModel.typeQuery(query)
                return true
            }
        })
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        super.onOptionsItemSelected(item)

        when (item.itemId) {
            R.id.write -> {
                conversationsViewModel.showCompose()
            }
        }

        return true
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
        conversationsViewModel.refresh()
    }

    private fun initializeView(view: View) {
        with(view.empty_view) {
            conversationsAdapter.emptyView = this
        }

        with(view.conversations) {
            setHasFixedSize(true)

            adapter = conversationsAdapter.apply {
                autoScrollToStart(this@with)

                onConversationClick = { conversationsViewModel.showConversation(it) }
                onConversationLongClick = { conversationsViewModel.showConversationDeletionConfirmation(activity, it) }
            }

            searchAdapter.apply {
                onSearchResultClick = { conversationsViewModel.showConversation(it) }
            }

            // Change adapter when search state changes.
            observe(conversationsViewModel.searching) {
                when(it) {
                    true -> {
                        Timber.i("Searching...")
                        adapter = searchAdapter
                    }
                    false -> {
                        Timber.i("Searching finished.")
                        adapter = conversationsAdapter
                    }
                }
            }
        }
    }
}
