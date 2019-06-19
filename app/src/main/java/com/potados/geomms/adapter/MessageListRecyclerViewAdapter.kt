package com.potados.geomms.adapter

import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView


import com.potados.geomms.fragment.MessageListFragment.OnListFragmentInteractionListener
import com.potados.geomms.R
import com.potados.geomms.dummy.DummyContent.DummyItem

import kotlinx.android.synthetic.main.fragment_message_list_item.view.*
import kotlinx.android.synthetic.main.friends_list_item.view.*

/**
 * [RecyclerView.Adapter] that can display a [DummyItem] and makes a call to the
 * specified [OnListFragmentInteractionListener].
 * TODO: Replace the implementation with code for your data type.
 */
class MessageListRecyclerViewAdapter(
    private val mValues: List<DummyItem>,
    private val mListener: OnListFragmentInteractionListener?
) : androidx.recyclerview.widget.RecyclerView.Adapter<MessageListRecyclerViewAdapter.ViewHolder>() {

    private val mOnClickListener: View.OnClickListener

    init {
        mOnClickListener = View.OnClickListener { v ->
            val item = v.tag as DummyItem
            // Notify the active callbacks interface (the activity, if the fragment is attached to
            // one) that an item has been selected.
            mListener?.onListFragmentInteraction(item)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.fragment_message_list_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = mValues[position]

        with (holder) {
            senderTextView.text = item.sender
            bodyTextView.text = item.body
            timeTextView.text = item.time
        }

        with(holder.mView) {
            tag = item
            setOnClickListener(mOnClickListener)
        }
    }

    override fun getItemCount(): Int = mValues.size

    inner class ViewHolder(val mView: View) : androidx.recyclerview.widget.RecyclerView.ViewHolder(mView) {
        val senderTextView: TextView = mView.sender_textview
        val bodyTextView: TextView = mView.body_textview
        val timeTextView: TextView = mView.time_textview

        override fun toString(): String {
            return super.toString() + " '" + senderTextView.text + "'"
        }
    }
}
