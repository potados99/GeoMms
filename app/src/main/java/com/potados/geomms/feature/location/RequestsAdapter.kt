package com.potados.geomms.feature.location

import android.view.LayoutInflater
import android.view.ViewGroup

import com.potados.geomms.R
import com.potados.geomms.common.base.BaseRealmAdapter
import com.potados.geomms.common.base.BaseViewHolder
import com.potados.geomms.model.ConnectionRequest
import kotlinx.android.synthetic.main.request_list_item.view.*
import org.koin.core.KoinComponent

class RequestsAdapter : BaseRealmAdapter<ConnectionRequest>(), KoinComponent {

    var onRequestClick: (ConnectionRequest) -> Unit = {}
    var onRequestLongClick: (ConnectionRequest) -> Unit = {}

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.request_list_item, parent, false)

        return BaseViewHolder(view).apply {
            view.setOnClickListener {
                val item = getItem(adapterPosition) ?: return@setOnClickListener
                onRequestClick(item)
            }
            view.setOnLongClickListener {
                val item = getItem(adapterPosition) ?: return@setOnLongClickListener false
                onRequestLongClick(item)
                true
            }
        }
    }

    override fun onBindViewHolder(holder: BaseViewHolder, position: Int) {
        val connection = getItem(position) ?: return
        val view = holder.containerView

        view.name.text = connection.recipient?.getDisplayName()
        view.avatar.setContact(connection.recipient)
    }

    interface RequestClickListener {
        fun onRequestClick(request: ConnectionRequest)
        fun onRequestLongClick(request: ConnectionRequest)
    }

}
