/*
 * Copyright (C) 2019 Song Byeong Jun <potados99@gmail.com>
 *
 * This file is part of GeoMms.
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

import android.view.MotionEvent
import android.view.View
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.recyclerview.widget.RecyclerView
import com.potados.geomms.common.widget.CustomBottomSheetBehavior

/**
 * Bottom sheet does not allow over two scrollable view as its child.
 * This will fix that.
 * Just call this for each recycler views in the sheet.
 */
fun RecyclerView.makeThisWorkInBottomSheet(sheet: View) {
    addOnItemTouchListener(object : RecyclerView.OnItemTouchListener {
        override fun onInterceptTouchEvent(rv: RecyclerView, e: MotionEvent): Boolean {
            val params = sheet.layoutParams

            if (params is CoordinatorLayout.LayoutParams) {
                val behavior = params.behavior
                if (behavior != null && behavior is CustomBottomSheetBehavior<*>)
                    behavior.setNestedScrollingChildRef(rv)
            }

            return false
        }
        override fun onTouchEvent(rv: RecyclerView, e: MotionEvent) {}
        override fun onRequestDisallowInterceptTouchEvent(disallowIntercept: Boolean) {}
    })
}