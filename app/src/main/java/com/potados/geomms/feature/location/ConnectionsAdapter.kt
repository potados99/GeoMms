package com.potados.geomms.feature.location

import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.potados.geomms.R
import com.potados.geomms.common.base.BaseRealmAdapter
import com.potados.geomms.common.base.BaseViewHolder
import com.potados.geomms.common.extension.setVisible
import com.potados.geomms.common.util.DateFormatter
import com.potados.geomms.model.Connection
import kotlinx.android.synthetic.main.connection_list_item.view.*
import org.koin.core.KoinComponent
import org.koin.core.inject
import java.util.*

class ConnectionsAdapter : BaseRealmAdapter<Connection>(), KoinComponent {

    private val dateFormatter: DateFormatter by inject()

    private val handler = Handler(Looper.getMainLooper())

    var onConnectionClick: (Connection) -> Unit = {}
    var onConnectionLongClick: (Connection) -> Unit = {}
    var onInfoClick: (Connection) -> Unit = {}

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.connection_list_item, parent, false)

        return TimerViewHolder(view).apply {
            view.setOnClickListener {
                val item = getItem(adapterPosition) ?: return@setOnClickListener
                onConnectionClick(item)
            }
            view.setOnLongClickListener {
                val item = getItem(adapterPosition) ?: return@setOnLongClickListener false
                onConnectionLongClick(item)
                true
            }
            view.info.setOnClickListener {
                val item = getItem(adapterPosition) ?: return@setOnClickListener
                onInfoClick(item)
            }
        }
    }

    override fun onBindViewHolder(holder: BaseViewHolder, position: Int) {
        val connection = getItem(position) ?: return
        val view = holder.containerView

        view.name.text = connection.recipient?.getDisplayName()
        view.avatar.setContact(connection.recipient)

        val alpha = if (connection.isTemporal) 0.5f else 1.0f
        val timeLeftVisibility = !connection.isTemporal

        view.name.alpha = alpha
        view.status.alpha = alpha
        view.duration_group.setVisible(timeLeftVisibility)

        view.status.text = when (connection.isTemporal) {
            true -> context.getString(R.string.connection_request_sent)

            false -> when (connection.lastUpdate) {
                0L -> {
                    context.getString(R.string.connection_location_not_available)
                }
                else -> {
                    val update = dateFormatter.getConversationTimestamp(connection.lastUpdate)
                    context.getString(R.string.connection_last_update, update)
                }
            }
        }

        view.time_label.text = when (connection.isTemporal) {
            true -> null
            false -> {
                val duration = dateFormatter.getDuration(connection.timeLeft, short = true)
                context.getString(R.string.connection_time_left, duration)
            }
        }

        (holder as TimerViewHolder).apply {
            timer?.cancel()

            if (timeLeftVisibility) {
                timer = Timer()
                timer?.schedule(object: TimerTask() {
                    override fun run() {
                        handler.post {
                            if (!connection.isValid) {
                                timer = null
                                return@post
                            }
                            with(view.time_progress) {
                                max = connection.duration.toInt()
                                progress = (connection.due - System.currentTimeMillis()).toInt()
                            }
                            with(view.time_label) {
                                val duration = dateFormatter.getDuration(connection.timeLeft, short = true)
                                text = context.getString(R.string.connection_time_left, duration)
                            }
                        }
                    }
                }, 0, 1000)
            }
        }
    }

    class TimerViewHolder(view: View) : BaseViewHolder(view) {
        var timer: Timer? = null
    }
}
