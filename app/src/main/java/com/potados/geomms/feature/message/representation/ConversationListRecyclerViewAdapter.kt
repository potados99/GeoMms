package com.potados.geomms.feature.message.representation

import android.graphics.Color
import android.graphics.Typeface
import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.mikhaellopez.circularimageview.CircularImageView

import com.potados.geomms.R
import com.potados.geomms.core.extension.serialize
import com.potados.geomms.feature.common.ContactRepository
import com.potados.geomms.core.util.ShortDate
import com.potados.geomms.feature.message.data.ConversationEntity

import kotlinx.android.synthetic.main.conversation_list_item.view.*
import org.koin.core.KoinComponent
import org.koin.core.inject
import kotlin.properties.Delegates

class ConversationListRecyclerViewAdapter(
    private val listener: ConversationClickListener
) : RecyclerView.Adapter<ConversationListRecyclerViewAdapter.ViewHolder>(), KoinComponent {

    private val contactRepo: ContactRepository by inject()

    internal var collection: List<ConversationEntity> by Delegates.observable(emptyList()) {
        _, _, _ -> notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder =
        ViewHolder(
            LayoutInflater
                .from(parent.context)
                .inflate(R.layout.conversation_list_item, parent, false)
        )

    override fun onBindViewHolder(holder: ViewHolder, position: Int) = holder.bind(collection[position])

    override fun getItemCount(): Int = collection.size

    interface ConversationClickListener {
        fun onConversationClicked(conversation: ConversationEntity)
    }

    inner class ViewHolder(val view: View) : RecyclerView.ViewHolder(view) {
        private val avatarImageView: CircularImageView = view.message_list_item_avatar
        private val senderTextView: TextView = view.message_list_item_sender_name
        private val bodyTextView: TextView = view.message_list_item_body
        private val timeTextView: TextView = view.message_list_item_time
        private val unreadIconView: CircularImageView = view.message_list_item_unread

        fun bind(item: ConversationEntity) {
            senderTextView.text = item.recipientNames(contactRepo).serialize()
            bodyTextView.text = item.snippet
            timeTextView.text = ShortDate.of(item.date)

            // TODO: 더미 이미지 교체.
            avatarImageView.setImageResource(R.drawable.avatar_default)

            if (item.isNotAllRead()) {
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
