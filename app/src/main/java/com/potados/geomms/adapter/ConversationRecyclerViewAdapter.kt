package com.potados.geomms.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.potados.geomms.R
import com.potados.geomms.data.ShortMessage
import com.potados.geomms.data.SmsThread
import com.potados.geomms.util.ShortDate
import kotlinx.android.synthetic.main.activity_conversation_message_item.view.*
import java.lang.RuntimeException

class ConversationRecyclerViewAdapter(
    private val messages: List<ShortMessage>
) : RecyclerView.Adapter<ConversationRecyclerViewAdapter.ViewHolder>() {


    override fun getItemCount(): Int = messages.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.activity_conversation_message_item, parent, false)

        return when (viewType) {
            TYPE_MESSAGE_RECEIVED -> ReceivedViewHolder(view)
            TYPE_MESSAGE_SENT -> SentViewHolder(view)
            else -> throw RuntimeException("Wrong viewType.")
        }
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val message = messages[position]

        if (position == 0) {
            holder.dateLayout.visibility = View.VISIBLE
            holder.dateTextView.text = ShortDate.of(message.date)
        }

        when(holder) {
            is SentViewHolder -> {
                holder.sentLayout.visibility = View.VISIBLE
                holder.sentBody.text = message.body
                holder.sentTime.text = ShortDate.of(message.date)
            }

            is ReceivedViewHolder -> {
                holder.receivedLayout.visibility = View.VISIBLE
                holder.receivedBody.text = message.body
                holder.receivedTime.text = ShortDate.of(message.date)
            }

            else -> {
                throw RuntimeException("Wrong view holder type.")
            }
        }

    }

    override fun getItemViewType(position: Int): Int =
        if (messages[position].isSent()) TYPE_MESSAGE_SENT
        else TYPE_MESSAGE_RECEIVED



    open inner class ViewHolder(val view: View): RecyclerView.ViewHolder(view) {
        val dateLayout: ConstraintLayout = view.message_resume_date_layout
        val dateTextView: TextView = view.message_resume_date
    }

    inner class SentViewHolder(view: View): ViewHolder(view) {
        val sentLayout: ConstraintLayout = view.message_sent_layout
        val sentBody: TextView = view.message_sent_body
        val sentTime: TextView = view.message_sent_time

    }

    inner class ReceivedViewHolder(view: View): ViewHolder(view) {
        val receivedLayout: ConstraintLayout = view.message_received_layout
        val receivedBody: TextView = view.message_received_body
        val receivedTime: TextView = view.message_received_time
    }

    companion object {
        const val TYPE_MESSAGE_SENT = 1
        const val TYPE_MESSAGE_RECEIVED = 2

        // TODO: 날짜 표시 조건 구현하기
    }
}