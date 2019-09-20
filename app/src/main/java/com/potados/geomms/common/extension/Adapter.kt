/**
 * Copyright (C) 2019 Song Byeong Jun and original authors
 *
 * This file is part of GeoMms.
 *
 * This software makes use of third-party patent which belongs to
 * KANG MOON KYOU and LEE GWI BONG:
 * System and Method for sharing service of location information
 * 10-1235884-0000 (2013.02.15)
 *
 * GeoMms is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * GeoMms is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with GeoMms.  If not, see <http://www.gnu.org/licenses/>.
 */


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