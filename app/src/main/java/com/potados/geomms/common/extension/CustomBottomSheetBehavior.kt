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