package com.potados.geomms.feature.location

import android.view.LayoutInflater
import android.view.ViewGroup

import com.potados.geomms.R
import com.potados.geomms.common.base.BaseRealmAdapter
import com.potados.geomms.common.base.BaseViewHolder
import com.potados.geomms.common.util.DateFormatter
import com.potados.geomms.model.Connection
import com.potados.geomms.model.ConnectionRequest
import io.realm.RealmRecyclerViewAdapter
import kotlinx.android.synthetic.main.request_item.view.*
import org.koin.core.KoinComponent
import org.koin.core.inject

class RequestsAdapter(
    private val listener: RequestClickListener
) :
    BaseRealmAdapter<ConnectionRequest>(),
    KoinComponent
{

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.request_item, parent, false)

        return BaseViewHolder(view).apply {
            view.setOnClickListener {
                val item = getItem(adapterPosition) ?: return@setOnClickListener
                listener.onRequestClick(item)
            }
        }
    }

    override fun onBindViewHolder(holder: BaseViewHolder, position: Int) {
        val connection = getItem(position) ?: return
        val view = holder.containerView

        view.title.text = connection.recipient?.getDisplayName()
        view.avatar.setContact(connection.recipient)
    }

    interface RequestClickListener {
        fun onRequestClick(request: ConnectionRequest)
    }

}