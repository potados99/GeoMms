package com.potados.geomms.common.base

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.potados.geomms.common.extension.setVisible
import io.realm.*

abstract class BaseRealmAdapter<T: RealmModel> : RealmRecyclerViewAdapter<T, BaseViewHolder>(null, true) {

    var emptyView: View? = null
        set(value) {
            if (field === value) return

            field = value
            value?.setVisible(data?.isLoaded == true && data?.isEmpty() == true)
        }

    /**
     * Opposite to emptyView.
     * Shown when data exists.
     * Useful when we want to hide something when data does not exist.
     */
    var companionView: View? = null
        set(value) {
            if (field === value) return

            field = value
            value?.setVisible(!(data?.isLoaded == true && data?.isEmpty() == true))
        }

    private val emptyListener: (OrderedRealmCollection<T>) -> Unit = { data ->
        emptyView?.setVisible(data.isLoaded && data.isEmpty())
        companionView?.setVisible(!(data.isLoaded && data.isEmpty()))
    }

    override fun updateData(data: OrderedRealmCollection<T>?) {
        if (getData() === data) return

        removeListener(getData())
        addListener(data)

        data?.run(emptyListener)

        super.updateData(data)
    }

    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        super.onAttachedToRecyclerView(recyclerView)
        addListener(data)
    }

    override fun onDetachedFromRecyclerView(recyclerView: RecyclerView) {
        super.onDetachedFromRecyclerView(recyclerView)
        removeListener(data)
    }

    private fun addListener(data: OrderedRealmCollection<T>?) {
        when (data) {
            is RealmResults<T> -> data.addChangeListener(emptyListener)
            is RealmList<T> -> data.addChangeListener(emptyListener)
        }
    }

    private fun removeListener(data: OrderedRealmCollection<T>?) {
        when (data) {
            is RealmResults<T> -> data.removeChangeListener(emptyListener)
            is RealmList<T> -> data.removeChangeListener(emptyListener)
        }
    }
}