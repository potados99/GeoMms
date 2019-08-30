package com.potados.geomms.feature.conversations

import android.view.LayoutInflater
import android.view.ViewGroup
import com.potados.geomms.R
import com.potados.geomms.common.base.BaseRealmAdapter
import com.potados.geomms.common.base.BaseViewHolder
import com.potados.geomms.common.extension.*
import com.potados.geomms.common.navigation.Navigator
import com.potados.geomms.common.util.DateFormatter
import com.potados.geomms.model.Conversation
import com.potados.geomms.usecase.SyncMessages
import io.realm.OrderedRealmCollection
import kotlinx.android.synthetic.main.conversation_list_item.view.*
import org.koin.core.KoinComponent
import org.koin.core.inject

class ConversationsAdapter(
    private val onLongClick: (Conversation) -> Unit
) : BaseRealmAdapter<Conversation>(), KoinComponent {

    private val dateFormatter: DateFormatter by inject()
    private val navigator: Navigator by inject()

    private val syncMessages: SyncMessages by inject()

    override fun updateData(data: OrderedRealmCollection<Conversation>?) {
        if (getData() === data) return

        super.updateData(data)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder {
        val view = LayoutInflater
            .from(parent.context)
            .inflate(R.layout.conversation_list_item, parent, false)

        if (viewType == VIEW_TYPE_UNREAD) {
            view.apply {
                title.setBold(true)
                snippet.setBold(true)
                snippet.setTextColorRes(R.color.textPrimary)
                unread.setVisible(true)
                date.setBold(true)
                date.setTextColorRes(R.color.textPrimary)
            }
        }

        return BaseViewHolder(view).apply {
            view.setOnClickListener {
                if (adapterPosition < 0) return@setOnClickListener
                val conversation = getItem(adapterPosition) ?: return@setOnClickListener

                navigator.showConversation(conversation.id)
            }
            view.setOnLongClickListener {
                if (adapterPosition < 0) return@setOnLongClickListener false
                val conversation = getItem(adapterPosition) ?: return@setOnLongClickListener false

                onLongClick(conversation)
                true
            }
        }
    }

    override fun onBindViewHolder(holder: BaseViewHolder, position: Int) {
        val conversation = getItem(position) ?: return
        val view = holder.containerView

        view.apply {
            avatars.contacts = conversation.recipients
            title.collapseEnabled = conversation.recipients.isNotEmpty()
            title.text = conversation.getTitle()
            date.text = dateFormatter.getConversationTimestamp(conversation.date).takeIf { conversation.date != 0L }
            snippet.text = conversation.snippet
        }
    }

    override fun getItemViewType(position: Int): Int =
        when (getItem(position)?.read) {
            true -> VIEW_TYPE_READ
            else -> VIEW_TYPE_UNREAD
        }

    companion object {
        private val VIEW_TYPE_READ = 1
        private val VIEW_TYPE_UNREAD = 0
    }
}
