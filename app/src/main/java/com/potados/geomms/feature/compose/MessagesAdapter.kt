package com.potados.geomms.feature.compose

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.potados.geomms.R
import com.potados.geomms.common.extension.isVisible
import com.potados.geomms.common.extension.setVisible
import com.potados.geomms.common.util.DateFormatter
import com.potados.geomms.model.Message
import com.potados.geomms.util.DateTime
import io.realm.RealmRecyclerViewAdapter
import kotlinx.android.synthetic.main.message_item.view.*
import org.koin.core.KoinComponent
import org.koin.core.inject
import java.lang.RuntimeException
import java.util.concurrent.TimeUnit

class MessagesAdapter:
    RealmRecyclerViewAdapter<Message, MessagesAdapter.ViewHolder>(null, true),
    KoinComponent
{
    private val dateFormatter: DateFormatter by inject()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.message_item, parent, false)

        return when (viewType) {
            TYPE_MESSAGE_RECEIVED -> ReceivedViewHolder(view)
            TYPE_MESSAGE_SENT -> SentViewHolder(view)
            else -> throw RuntimeException("Wrong viewType.")
        }
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val message = getItem(position) ?: return
        val previous = if (position == 0) null else getItem(position - 1)

        if (position == 0) {
            holder.dateLayout.visibility = View.VISIBLE
            holder.dateTextView.text = dateFormatter.getMessageTimestamp(message.date)
        }

        val timeSincePrevious = TimeUnit.MILLISECONDS.toMinutes(message.date - (previous?.date ?: 0))

        val timeText = dateFormatter.getMessageTimestamp(message.date)
        val timeVisibility = (timeSincePrevious >= BubbleUtils.TIMESTAMP_THRESHOLD)

        when(holder) {
            is SentViewHolder -> {
                holder.sentLayout.visibility = View.VISIBLE
                holder.sentBody.text = message.body
                holder.sentTime.text = timeText
                holder.sentTime.setVisible(timeVisibility)
            }

            is ReceivedViewHolder -> {
                holder.receivedLayout.visibility = View.VISIBLE
                holder.receivedBody.text = message.body
                holder.receivedTime.text = timeText
                holder.receivedTime.setVisible(timeVisibility)
            }

            else -> {
                throw RuntimeException("Wrong view holder type.")
            }
        }

    }

    override fun getItemViewType(position: Int): Int =
        if (getItem(position)!!.isMe()) TYPE_MESSAGE_SENT
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
        // -> 함: 20190812
    }
}