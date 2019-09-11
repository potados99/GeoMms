package com.potados.geomms.feature.conversations

import android.content.Context
import android.text.SpannableString
import android.text.Spanned
import android.text.style.BackgroundColorSpan
import android.view.LayoutInflater
import android.view.ViewGroup
import com.potados.geomms.R
import com.potados.geomms.common.base.BaseAdapter
import com.potados.geomms.common.base.BaseViewHolder
import com.potados.geomms.common.extension.resolveThemeColor
import com.potados.geomms.common.extension.setVisible
import com.potados.geomms.common.navigation.Navigator
import com.potados.geomms.common.util.DateFormatter
import com.potados.geomms.model.SearchResult
import kotlinx.android.synthetic.main.search_list_item.view.*
import org.koin.core.KoinComponent
import org.koin.core.inject

class SearchAdapter : BaseAdapter<SearchResult>(), KoinComponent {

    private val context: Context by inject()
    private val dateFormatter: DateFormatter by inject()

    var onSearchResultClick: (SearchResult) -> Unit = {}

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val view = layoutInflater.inflate(R.layout.search_list_item, parent, false)
        return BaseViewHolder(view).apply {
            view.setOnClickListener {
                val result = getItem(adapterPosition) ?: return@setOnClickListener
                onSearchResultClick(result)
            }
        }
    }

    override fun onBindViewHolder(viewHolder: BaseViewHolder, position: Int) {
        val previous = data.getOrNull(position - 1)
        val result = getItem(position) ?: return
        val view = viewHolder.containerView

        view.results_header.setVisible(result.messages > 0 && previous?.messages == 0)

        val query = result.query
        val title = SpannableString(result.conversation.getTitle())
        var index = title.indexOf(query, ignoreCase = true)

        while (index >= 0) {
            title.setSpan(
                BackgroundColorSpan(context.resolveThemeColor(R.attr.tintPrimary)),
                index,
                index + query.length,
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
            )
            index = title.indexOf(query, index + query.length, true)
        }
        view.title.text = title

        view.avatars.contacts = result.conversation.recipients

        when (result.messages == 0) {
            true -> {
                view.date.setVisible(true)
                view.date.text = dateFormatter.getConversationTimestamp(result.conversation.date)
                view.snippet.text = result.conversation.snippet
            }

            false -> {
                view.date.setVisible(false)
                view.snippet.text = context.getString(R.string.main_message_results, result.messages)
            }
        }
    }

    override fun areItemsTheSame(old: SearchResult, new: SearchResult): Boolean {
        return old.conversation.id == new.conversation.id && old.messages > 0 == new.messages > 0
    }

    override fun areContentsTheSame(old: SearchResult, new: SearchResult): Boolean {
        return old.query == new.query && // Queries are the same
                old.conversation.id == new.conversation.id // Conversation id is the same
                && old.messages == new.messages // Result count is the same
    }

    interface SearchResultClickListener {
        fun onSearchResultClick(result: SearchResult)
    }
}