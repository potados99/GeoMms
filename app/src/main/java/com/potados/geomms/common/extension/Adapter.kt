
package com.potados.geomms.common.extension

import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import timber.log.Timber

fun RecyclerView.Adapter<*>.autoScrollToStart(recyclerView: RecyclerView) {
    registerAdapterDataObserver(object : RecyclerView.AdapterDataObserver() {
        override fun onItemRangeInserted(positionStart: Int, itemCount: Int) {
            Timber.v("range: ($positionStart..${positionStart + itemCount})")

            val layoutManager = recyclerView.layoutManager as? LinearLayoutManager ?: return

            if (layoutManager.stackFromEnd) {
                if (positionStart > 0) {
                    notifyItemChanged(positionStart - 1)
                }

                val lastPosition = layoutManager.findLastVisibleItemPosition()
                if (positionStart >= getItemCount() - 1 && lastPosition == positionStart - 1) {
                    recyclerView.scrollToPosition(positionStart)
                }
            } else {
                val firstVisiblePosition = layoutManager.findFirstVisibleItemPosition()
                if (firstVisiblePosition == 0) {
                    recyclerView.scrollToPosition(positionStart)
                }
            }
        }

        override fun onItemRangeChanged(positionStart: Int, itemCount: Int) {
            Timber.v("range: ($positionStart..${positionStart + itemCount})")

            val layoutManager = recyclerView.layoutManager as? LinearLayoutManager ?: return

            if (!layoutManager.stackFromEnd) {
                onItemRangeInserted(positionStart, itemCount)
            }
        }
    })
}