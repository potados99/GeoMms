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
import com.potados.geomms.common.util.DateFormatter

import com.potados.geomms.model.Conversation
import com.potados.geomms.util.DateTime
import io.realm.OrderedRealmCollection
import io.realm.RealmRecyclerViewAdapter

import kotlinx.android.synthetic.main.conversations_item.view.*
import org.koin.core.KoinComponent
import org.koin.core.inject

class ConversationsAdapter(
    private val listener: ConversationClickListener
) : RealmRecyclerViewAdapter<Conversation, ConversationsAdapter.ViewHolder>(null, true),
    KoinComponent
{
    private val dateFormatter: DateFormatter by inject()

    override fun updateData(data: OrderedRealmCollection<Conversation>?) {
        if (getData() === data) return

        super.updateData(data)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater
                .from(parent.context)
                .inflate(R.layout.conversations_item, parent, false)

        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        getItem(position)?.let(holder::bind)
    }

    interface ConversationClickListener {
        fun onConversationClicked(conversation: Conversation)
    }

    inner class ViewHolder(val view: View) : RecyclerView.ViewHolder(view) {
        private val avatarImageView: CircularImageView = view.message_list_item_avatar
        private val senderTextView: TextView = view.message_list_item_sender_name
        private val bodyTextView: TextView = view.message_list_item_body
        private val timeTextView: TextView = view.message_list_item_time
        private val unreadIconView: CircularImageView = view.message_list_item_unread

        fun bind(item: Conversation) {
            senderTextView.text = item.getTitle()
            bodyTextView.text = item.snippet
            timeTextView.text = dateFormatter.getConversationTimestamp(item.date)

            avatarImageView.setImageResource(R.drawable.avatar_default)

            if (!item.read) {
                unreadIconView.visibility = View.VISIBLE
                bodyTextView.setTypeface(bodyTextView.typeface, Typeface.BOLD)
                bodyTextView.setTextColor(Color.BLACK)
            }

            view.setOnClickListener {
                listener.onConversationClicked(item)
            }
        }
    }
}
