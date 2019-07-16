package com.potados.geomms.adapter

import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView

import com.potados.geomms.R
import com.potados.geomms.data.entity.LocationSupportConnection

import kotlinx.android.synthetic.main.fragment_map_friends_list_item.view.*

class FriendsRecyclerViewAdapter(
    private val connections: List<LocationSupportConnection>,
    private val listener: FriendClickListener
) : RecyclerView.Adapter<FriendsRecyclerViewAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.fragment_map_friends_list_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = connections[position]

        with (holder) {
            nameTextView.text = item.person.name
            distanceTextVeiw.text = item.lastSeenDistance?.toShortenString() ?: "-"
            lastUpdateTextView.text = item.lastReceivedTime?.durationUntilNow()?.toShortenString() ?: "-"
        }

        with(holder.view) {
            tag = item
            setOnClickListener {
                listener.onFriendClicked()
            }
            friends_list_item_call_button.setOnClickListener {
                listener.onFriendCallClicked()
            }
        }

    }

    override fun getItemCount(): Int = connections.size

    interface FriendClickListener {
        fun onFriendClicked()
        fun onFriendCallClicked()
        fun onFriendRequestUpdateClicked()
    }

    inner class ViewHolder(val view: View) : RecyclerView.ViewHolder(view) {
        val nameTextView: TextView = view.friends_list_item_name_textview
        val distanceTextVeiw: TextView = view.friends_list_item_distance_textview
        val lastUpdateTextView: TextView = view.friends_list_item_last_update_textview
    }
}
