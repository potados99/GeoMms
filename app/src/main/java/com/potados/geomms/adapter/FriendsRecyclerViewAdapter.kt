package com.potados.geomms.adapter

import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView

import com.potados.geomms.R
import com.potados.geomms.dummy.DummyContent.DummyItem

import kotlinx.android.synthetic.main.fragment_map_friends_list_item.view.*

class FriendsRecyclerViewAdapter(
    private val mValues: List<DummyItem>
) : RecyclerView.Adapter<FriendsRecyclerViewAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.fragment_map_friends_list_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = mValues[position]

        with (holder) {
            nameTextView.text = item.name
            timeTextView.text = item.elapse
        }

        with(holder.mView) {
            tag = item
        }
    }

    override fun getItemCount(): Int = mValues.size

    inner class ViewHolder(val mView: View) : androidx.recyclerview.widget.RecyclerView.ViewHolder(mView) {
        val nameTextView: TextView = mView.friends_list_item_name
        val timeTextView: TextView = mView.friends_list_item_last_update

        override fun toString(): String {
            return super.toString() + " '" + nameTextView.text + "'"
        }
    }
}
