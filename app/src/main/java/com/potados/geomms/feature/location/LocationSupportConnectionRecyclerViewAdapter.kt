package com.potados.geomms.feature.location

import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment

import com.potados.geomms.R
import com.potados.geomms.feature.location.data.LocationSupportConnection

import kotlinx.android.synthetic.main.fragment_map_friends_list_item.view.*
import java.util.*
import kotlin.properties.Delegates

class LocationSupportConnectionRecyclerViewAdapter(
    private val listener: FriendClickListener
) : RecyclerView.Adapter<LocationSupportConnectionRecyclerViewAdapter.ViewHolder>() {

    internal var collection: List<LocationSupportConnection> by Delegates.observable(emptyList()) {
            _, _, _ -> notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.fragment_map_friends_list_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = collection[position]

        with (holder) {
            nameTextView.text = item.person.displayName
            distanceTextVeiw.text = item.lastSeenDistance?.toShortenString() ?: "-"

            val timerTask = object: TimerTask() {
                override fun run() {
                    (listener as Fragment).activity?.runOnUiThread {
                        lastUpdateTextView.text = item.lastReceivedTime?.durationUntilNow()?.toShortenString() ?: "-"
                    }
                }
            }

            Timer().schedule(timerTask, 0, 1000)
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

    override fun getItemCount(): Int = collection.size

    fun updateItems() {
        for (i in 0 until itemCount) {

        }
    }


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
