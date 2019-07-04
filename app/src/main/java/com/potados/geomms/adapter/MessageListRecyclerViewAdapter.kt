package com.potados.geomms.adapter

import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.ColorDrawable
import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.mikhaellopez.circularimageview.CircularImageView

import com.potados.geomms.R
import com.potados.geomms.data.ShortMessage
import com.potados.geomms.util.Notify
import com.potados.geomms.util.ShortDate

import kotlinx.android.synthetic.main.fragment_message_list_item.view.*

class MessageListRecyclerViewAdapter(
    private val conversationHeads: List<ShortMessage>
) : RecyclerView.Adapter<MessageListRecyclerViewAdapter.ViewHolder>() {


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
            tag = smsItem

            setOnClickListener {
                Notify.short(context, "Thread id: ${smsItem.threadId}")
            }
        }
    }

    override fun getItemCount(): Int = conversationHeads.size

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
