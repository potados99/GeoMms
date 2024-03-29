/*
 * Copyright (C) 2019-2020 Song Byeong Jun <potados99@gmail.com>
 *
 * This file is part of GeoMms.
 *
 * GeoMms is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * GeoMms is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with GeoMms.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.potados.geomms.feature.conversations

import android.os.Bundle
import android.view.*
import androidx.appcompat.widget.SearchView
import com.potados.geomms.R
import com.potados.geomms.common.base.NavigationFragment
import com.potados.geomms.common.extension.autoScrollToStart
import com.potados.geomms.common.extension.getViewModel
import com.potados.geomms.common.extension.observe
import com.potados.geomms.databinding.ConversationsFragmentBinding
import com.potados.geomms.repository.SyncRepository
import kotlinx.android.synthetic.main.conversations_fragment.view.*
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
