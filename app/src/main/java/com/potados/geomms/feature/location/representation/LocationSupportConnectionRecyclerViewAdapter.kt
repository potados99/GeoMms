package com.potados.geomms.feature.location.representation

import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment

import com.potados.geomms.R
import com.potados.geomms.feature.location.data.LSConnection

import kotlinx.android.synthetic.main.fragment_map_friends_list_item.view.*
import java.util.*
import kotlin.properties.Delegates

class LocationSupportConnectionRecyclerViewAdapter(
    private val listener: FriendClickListener
) : RecyclerView.Adapter<LocationSupportConnectionRecyclerViewAdapter.ViewHolder>() {

    internal var collection: List<LSConnection> by Delegates.observable(emptyList()) {
            _, _, _ -> notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.fragment_map_friends_list_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(collection[position])
    }

    override fun getItemCount(): Int = collection.size

    interface FriendClickListener {
        fun onFriendClicked(connection: LSConnection)
        fun onFriendCallClicked()
        fun onFriendRequestUpdateClicked()
    }

    inner class ViewHolder(val view: View) : RecyclerView.ViewHolder(view) {
        private val nameTextView: TextView = view.friends_list_item_name_textview
        private val statusTextView: TextView = view.friends_list_item_status_textview

        fun bind(item: LSConnection) {
            nameTextView.text = item.person.name
            statusTextView.text = item.currentDistance?.toShortenString() ?: "-"

            val timerTask = object: TimerTask() {
                override fun run() {
                    (listener as Fragment).activity?.runOnUiThread {
                        statusTextView.text = item.lastReceivedTime?.durationUntilNow()?.toShortenString() ?: "-"
                    }
                }
            }

            java.util.Timer().schedule(timerTask, 0, 1000)

            with(view) {
                tag = item
                setOnClickListener {
                    listener.onFriendClicked(item)
                }
                friends_list_item_call_button.setOnClickListener {
                    listener.onFriendCallClicked()
                }
            }

        }
    }
}
