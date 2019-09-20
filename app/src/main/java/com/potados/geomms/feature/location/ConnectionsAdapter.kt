package com.potados.geomms.feature.location

import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.potados.geomms.R
import com.potados.geomms.common.base.BaseRealmAdapter
import com.potados.geomms.common.base.BaseViewHolder
import com.potados.geomms.common.extension.doEvery
import com.potados.geomms.common.extension.isVisible
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
    var onRefreshClick: (Connection) -> Unit = {}

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
            view.refresh_button.setOnClickListener {
                val item = getItem(adapterPosition) ?: return@setOnClickListener
                onRefreshClick(item)
            }
        }
    }

    override fun onBindViewHolder(holder: BaseViewHolder, position: Int) {
        val view = holder.containerView
        val item = getItem(position) ?: return

        view.name.text = item.recipient?.getDisplayName()
        view.avatar.setContact(item.recipient)

        val alpha = if (item.isTemporal) 0.5f else 1.0f
        val timeLeftVisibility = !item.isTemporal
        val refreshVisibility = !item.isTemporal

        view.name.alpha = alpha
        view.status.alpha = alpha
        view.duration_group.setVisible(timeLeftVisibility)

        view.status.text = when (item.isTemporal) {
            true -> context.getString(R.string.connection_request_sent)

            false -> when (item.lastUpdate) {
                0L -> {
                    context.getString(R.string.connection_location_not_available)
                }
                else -> {
                    val update = dateFormatter.getConversationTimestamp(item.lastUpdate)
                    context.getString(R.string.connection_last_update, update)
                }
            }
        }

        view.time_label.text = when (item.isTemporal) {
            true -> null
            false -> {
                val duration = dateFormatter.getDuration(item.timeLeft, short = true)
                context.getString(R.string.connection_time_left, duration)
            }
        }

        view.refresh_button.isVisible = refreshVisibility

        // Refresh left time every second.
        (holder as TimerViewHolder).apply {
            timer?.cancel()

            if (timeLeftVisibility) {
                timer = Timer()
                timer?.doEvery(1000) {
                    handler.post {
                        if (!item.isValid) {
                            timer = null
                            return@post
                        }

                        val duration = dateFormatter.getDuration(item.timeLeft, short = true)

                        with(view.time_label) {
                            text = context.getString(R.string.connection_time_left, duration)
                        }
                    }
                }
            }
        }
    }

    class TimerViewHolder(view: View) : BaseViewHolder(view) {
        var timer: Timer? = null
    }
}
