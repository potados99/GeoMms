package com.potados.geomms.adapter

import android.graphics.Color
import android.graphics.Typeface
import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.mikhaellopez.circularimageview.CircularImageView

import com.potados.geomms.R
import com.potados.geomms.data.repository.ContactRepository
import com.potados.geomms.data.entity.SmsThread
import com.potados.geomms.core.util.ShortDate

import kotlinx.android.synthetic.main.fragment_conversation_list_item.view.*
import org.koin.core.KoinComponent
import org.koin.core.inject
import kotlin.properties.Delegates

class ConversationListRecyclerViewAdapter(
    private val listener: ConversationClickListener
) : RecyclerView.Adapter<ConversationListRecyclerViewAdapter.ViewHolder>(), KoinComponent {

    private val contactRepo: ContactRepository by inject()

    internal var collection: List<SmsThread> by Delegates.observable(emptyList()) {
        _, _, _ -> notifyDataSetChanged()
    }

    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        super.onAttachedToRecyclerView(recyclerView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.fragment_conversation_list_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val threadItem = collection[position]

        with (holder) {
            senderTextView.text = threadItem.getRecipientString(contactRepo)
            bodyTextView.text = threadItem.snippet
            timeTextView.text = ShortDate.of(threadItem.date)

            // TODO: 더미 이미지 교체.
            avatarImageView.setImageResource(R.drawable.avatar_default)

            if (threadItem.isNotAllRead()) {
                unreadIconView.visibility = View.VISIBLE
                bodyTextView.setTypeface(bodyTextView.typeface, Typeface.BOLD)
                bodyTextView.setTextColor(Color.BLACK)
            }
        }

        with(holder.view) {
            setOnClickListener {
                listener.onConversationClicked(threadItem)
            }
        }
    }

    override fun getItemCount(): Int = collection.size

    interface ConversationClickListener {
        fun onConversationClicked(conversation: SmsThread)
    }

    inner class ViewHolder(val view: View) : RecyclerView.ViewHolder(view) {
        val avatarImageView: CircularImageView = view.message_list_item_avatar
        val senderTextView: TextView = view.message_list_item_sender_name
        val bodyTextView: TextView = view.message_list_item_body
        val timeTextView: TextView = view.message_list_item_time
        val unreadIconView: CircularImageView = view.message_list_item_unread

        override fun toString(): String {
            return super.toString() + " '" + senderTextView.text + "'"
        }
    }


}
