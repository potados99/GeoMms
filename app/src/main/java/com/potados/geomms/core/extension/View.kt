package com.potados.geomms.core.extension

import android.view.View
import com.google.android.material.bottomsheet.BottomSheetBehavior

fun View.hideSheet() {
    BottomSheetBehavior.from(this).state = BottomSheetBehavior.STATE_HIDDEN
}

fun View.collapseSheet() {
    BottomSheetBehavior.from(this).state = BottomSheetBehavior.STATE_COLLAPSED
}

fun View.expandSheet() {
    BottomSheetBehavior.from(this).state = BottomSheetBehavior.STATE_EXPANDED
}

fun View.sheetState() = BottomSheetBehavior.from(this).state

fun View.toggleSheet() {
    BottomSheetBehavior.from(this).apply {
        state = when (state) {
            BottomSheetBehavior.STATE_EXPANDED -> BottomSheetBehavior.STATE_HALF_EXPANDED
            BottomSheetBehavior.STATE_HALF_EXPANDED -> BottomSheetBehavior.STATE_COLLAPSED
            BottomSheetBehavior.STATE_COLLAPSED -> BottomSheetBehavior.STATE_HALF_EXPANDED
            else -> BottomSheetBehavior.STATE_HALF_EXPANDED
        }
    }
}

fun View.bottomSheetBehavior() = BottomSheetBehavior.from(this)