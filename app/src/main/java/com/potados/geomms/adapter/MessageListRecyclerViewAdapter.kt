package com.potados.geomms.adapter

import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
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
import com.potados.geomms.util.ShortDate

import kotlinx.android.synthetic.main.fragment_message_list_item.view.*

class MessageListRecyclerViewAdapter(
    private val conversationHeads: List<ShortMessage>,
    private val listener: ConversationClickListener
) : RecyclerView.Adapter<MessageListRecyclerViewAdapter.ViewHolder>() {


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
            senderTextView.text = smsItem.address.trim()
            bodyTextView.text = smsItem.body.trim()
            timeTextView.text = ShortDate.of(smsItem.date).trim()

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
        val senderTextView: TextView = view.message_list_item_sender_name
        val bodyTextView: TextView = view.message_list_item_body
        val timeTextView: TextView = view.message_list_item_time
        val unreadIconView: CircularImageView = view.message_list_item_unread

        override fun toString(): String {
            return super.toString() + " '" + senderTextView.text + "'"
        }
    }
}
