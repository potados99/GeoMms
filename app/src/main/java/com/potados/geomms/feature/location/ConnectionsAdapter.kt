package com.potados.geomms.feature.location

import android.view.LayoutInflater
import android.view.ViewGroup

import com.potados.geomms.R
import com.potados.geomms.common.base.BaseRealmAdapter
import com.potados.geomms.common.base.BaseViewHolder
import com.potados.geomms.common.util.DateFormatter
import com.potados.geomms.model.Connection
import kotlinx.android.synthetic.main.connection_list_item.view.*
import org.koin.core.KoinComponent
import org.koin.core.inject

class ConnectionsAdapter(
    private val listener: ConnectionClickListener
) :
    BaseRealmAdapter<Connection>(),
    KoinComponent
{

    private val dateFormatter: DateFormatter by inject()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.connection_list_item, parent, false)

        return BaseViewHolder(view).apply {
            view.setOnClickListener {
                val item = getItem(adapterPosition) ?: return@setOnClickListener
                listener.onConnectionClick(item)
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
    }

    interface ConnectionClickListener {
        fun onConnectionClick(connection: Connection)
    }

}
