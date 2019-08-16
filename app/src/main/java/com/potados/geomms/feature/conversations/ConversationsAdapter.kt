package com.potados.geomms.feature.conversations

import android.graphics.Color
import android.graphics.Typeface
import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.mikhaellopez.circularimageview.CircularImageView
import com.potados.geomms.R
import com.potados.geomms.common.base.BaseRealmAdapter
import com.potados.geomms.common.base.BaseViewHolder
import com.potados.geomms.common.extension.*
import com.potados.geomms.common.navigation.Navigator
import com.potados.geomms.common.util.DateFormatter
import com.potados.geomms.model.Conversation
import com.potados.geomms.util.DateTime
import io.realm.OrderedRealmCollection
import io.realm.RealmRecyclerViewAdapter
import kotlinx.android.synthetic.main.conversation_item.view.*
import org.koin.core.KoinComponent
import org.koin.core.inject

class ConversationsAdapter : BaseRealmAdapter<Conversation>(), KoinComponent {

    private val dateFormatter: DateFormatter by inject()
    private val navigator: Navigator by inject()

    override fun updateData(data: OrderedRealmCollection<Conversation>?) {
        if (getData() === data) return

        super.updateData(data)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder {

        val view = LayoutInflater
            .from(parent.context)
            .inflate(R.layout.conversation_item, parent, false)

        if (viewType == VIEW_TYPE_UNREAD) {
            view.apply {
                title.setBold(true)
                snippet.setBold(true)
                snippet.setTextColorRes(R.color.primaryText) // TODO null theme
                unread.setVisible(true)
                unread.setTintRes(R.color.primary)
                date.setBold(true)
                date.setTextColorRes(R.color.primary)
            }
        }

        return BaseViewHolder(view).apply {
            view.setOnClickListener {
                val conversation = getItem(adapterPosition) ?: return@setOnClickListener

                navigator.showComposeActivity(conversation)
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
            date.text = dateFormatter.getConversationTimestamp(conversation.date)
            snippet.text = conversation.snippet
        }
    }

    override fun getItemViewType(position: Int): Int =
        when (getItem(position)?.read) {
            true -> VIEW_TYPE_READ
            else -> VIEW_TYPE_UNREAD
        }

    interface ConversationClickListener {
        fun onConversationClicked(conversation: Conversation)
    }

    companion object {
        private val VIEW_TYPE_READ = 1
        private val VIEW_TYPE_UNREAD = 0
    }
}
