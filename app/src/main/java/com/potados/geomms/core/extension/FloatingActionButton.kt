package com.potados.geomms.core.extension

import android.view.View
import com.google.android.material.floatingactionbutton.FloatingActionButton

@SuppressWarnings("RestrictedApi")
fun FloatingActionButton.forceHide() {
    this.visibility = View.GONE
}

fun FloatingActionButton.scale(factor: Float) {
    this.isClickable = (factor != 0f)

    this.scaleX = factor
    this.scaleY = factor
}