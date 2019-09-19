package com.potados.geomms.common.extension

import android.view.View
import com.potados.geomms.common.widget.CustomBottomSheetBehavior

/**
 * Set callback.
 */
fun CustomBottomSheetBehavior<*>.setCallback(
    onStateChanged: (Int) -> Unit = {},
    onSlide: (Float) -> Unit = {}
): CustomBottomSheetBehavior.BottomSheetCallback {
    val callback = object: CustomBottomSheetBehavior.BottomSheetCallback() {
        override fun onStateChanged(bottomSheet: View, newState: Int) {
            onStateChanged(newState)
        }

        override fun onSlide(bottomSheet: View, slideOffset: Float) {
            onSlide(slideOffset)
        }
    }
    bottomSheetCallback = callback

    return callback
}

/**
 * Add callback to existing callback(s).
 */
fun CustomBottomSheetBehavior<*>.addCallback(
    onStateChanged: (Int) -> Unit = {},
    onSlide: (Float) -> Unit = {}
): CustomBottomSheetBehavior.BottomSheetCallback {
    if (bottomSheetCallback == null) {
        // Works only when appending to the existing one.
        return setCallback(onStateChanged, onSlide)
    }

    val existingCallback = bottomSheetCallback
    val newCallback = object: CustomBottomSheetBehavior.BottomSheetCallback() {
        override fun onStateChanged(bottomSheet: View, newState: Int) {
            existingCallback.onStateChanged(bottomSheet, newState)
            onStateChanged(newState)
        }

        override fun onSlide(bottomSheet: View, slideOffset: Float) {
            existingCallback.onSlide(bottomSheet, slideOffset)
            onSlide(slideOffset)
        }
    }

    bottomSheetCallback = newCallback

    return newCallback
}