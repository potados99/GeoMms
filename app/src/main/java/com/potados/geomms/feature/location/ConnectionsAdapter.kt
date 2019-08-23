package com.potados.geomms.feature.location

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import com.potados.geomms.R
import com.potados.geomms.common.base.BaseRealmAdapter
import com.potados.geomms.common.base.BaseViewHolder
import com.potados.geomms.common.util.DateFormatter
import com.potados.geomms.model.Connection
import io.realm.OrderedRealmCollection
import io.realm.Realm
import io.realm.RealmResults
import kotlinx.android.synthetic.main.connection_list_item.view.*
import org.koin.core.KoinComponent
import org.koin.core.inject
import timber.log.Timber
import java.util.*

class ConnectionsAdapter(
    private val listener: ConnectionClickListener
) :
    BaseRealmAdapter<Connection>(),
    KoinComponent
{

    private val dateFormatter: DateFormatter by inject()

    private val handler = Handler(Looper.getMainLooper())

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.connection_list_item, parent, false)

        return TimerViewHolder(view).apply {
            view.setOnClickListener {
                val item = getItem(adapterPosition) ?: return@setOnClickListener
                listener.onConnectionClick(item)
            }
            view.setOnLongClickListener {
                val item = getItem(adapterPosition) ?: return@setOnLongClickListener false
                listener.onConnectionLongClick(item)
                true
            }
        }
    }

    override fun onBindViewHolder(holder: BaseViewHolder, position: Int) {
        val connection = getItem(position) ?: return
        val view = holder.containerView

        view.name.text = connection.recipient?.getDisplayName()
        view.avatar.setContact(connection.recipient)
        view.status.text = when (connection.lastUpdate == 0L) {
            true -> "-"
            else -> dateFormatter.getConversationTimestamp(connection.lastUpdate)
        }

        (holder as TimerViewHolder).apply {
            timer?.cancel()
            timer = Timer()

            timer?.schedule(object: TimerTask() {
                override fun run() {
                    handler.post {
                        if (!connection.isValid) {
                            timer = null
                            return@post
                        }
                        with(view.time_left) {
                            max = connection.duration.toInt()
                            progress = (connection.due - System.currentTimeMillis()).toInt()
                        }
                    }
                }
            }, 0, 1000)
        }
    }

    class TimerViewHolder(view: View) : BaseViewHolder(view) {
        var timer: Timer? = null
    }

    interface ConnectionClickListener {
        fun onConnectionClick(connection: Connection)
        fun onConnectionLongClick(connection: Connection)
    }
}
