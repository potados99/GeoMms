package com.potados.geomms.adapter

import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.Drawable
import android.util.Log
import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.mikhaellopez.circularimageview.CircularImageView

import com.potados.geomms.R
import com.potados.geomms.activity.ConversationActivity
import com.potados.geomms.data.ShortMessage
import com.potados.geomms.util.ContactHelper
import com.potados.geomms.util.ShortDate

import kotlinx.android.synthetic.main.fragment_message_list_item.view.*
import org.koin.core.KoinComponent
import org.koin.core.inject

class MessageListRecyclerViewAdapter(
    private val conversationHeads: List<ShortMessage>,
    private val listener: ConversationClickListener
) : RecyclerView.Adapter<MessageListRecyclerViewAdapter.ViewHolder>(), KoinComponent {

    val resolver: ContentResolver by inject()

    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        super.onAttachedToRecyclerView(recyclerView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.fragment_message_list_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val smsItem = conversationHeads[position]

        with (holder) {
            Log.d("yyyyyyyyyyyy", smsItem.address)
            senderTextView.text = ContactHelper.getContactName(resolver, smsItem.address) ?: smsItem.address
            bodyTextView.text = smsItem.body
            timeTextView.text = ShortDate.of(smsItem.date)

            // TODO: 더미 이미지 교체.
            avatarImageView.setImageResource(R.drawable.avatar_default)

            if (smsItem.isNotRead()) {
                unreadIconView.visibility = View.VISIBLE
                bodyTextView.setTypeface(bodyTextView.typeface, Typeface.BOLD)
                bodyTextView.setTextColor(Color.BLACK)
            }
        }

        with(holder.view) {
            setOnClickListener {
                listener.onConversationClicked(smsItem)
            }
        }
    }

    override fun getItemCount(): Int = conversationHeads.size

    interface ConversationClickListener {
        fun onConversationClicked(conversationHead: ShortMessage)
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
