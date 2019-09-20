package com.potados.geomms.common.extension

import android.view.MotionEvent
import android.view.View
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.recyclerview.widget.RecyclerView
import com.potados.geomms.common.widget.CustomBottomSheetBehavior
import timber.log.Timber

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
                Timber.e("sex!")
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